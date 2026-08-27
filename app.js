// ================= CONFIGURATION & CONSTANTS =================
let API_BASE_URL = window.location.origin;

// Auto-detect localhost or direct filesystem preview
if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' || window.location.protocol === 'file:') {
    API_BASE_URL = "http://172.23.50.24:8000";
}

// State variables
let currentUser = null;
let currentToken = localStorage.getItem("token") || null;
let allScans = [];
let localCoords = { lat: 13.0827, lon: 80.2707 }; // Chennai default
let selectedFile = null;
let currentLanguage = 'en';
let onboardingIndex = 0;
let webcamStream = null;

// ================= TOAST NOTIFICATION SYSTEM =================
function showToast(message, type = "info") {
    // Dynamically create container if it does not exist
    let container = document.getElementById("toast-container");
    if (!container) {
        container = document.createElement("div");
        container.id = "toast-container";
        container.style.position = "fixed";
        container.style.bottom = "84px";
        container.style.right = "20px";
        container.style.display = "flex";
        container.style.flexDirection = "column";
        container.style.gap = "10px";
        container.style.zIndex = "99999";
        document.body.appendChild(container);
    }
    
    const toast = document.createElement("div");
    toast.className = `toast-message`;
    toast.style.background = "rgba(13, 18, 33, 0.95)";
    toast.style.backdropFilter = "blur(10px)";
    toast.style.color = "#fff";
    toast.style.padding = "12px 20px";
    toast.style.borderRadius = "8px";
    toast.style.fontSize = "0.85rem";
    toast.style.fontWeight = "600";
    toast.style.border = "1px solid rgba(255, 255, 255, 0.1)";
    toast.style.boxShadow = "0 8px 24px rgba(0, 0, 0, 0.3)";
    toast.style.transition = "all 0.3s ease";
    toast.style.opacity = "0";
    toast.style.transform = "translateY(10px)";
    toast.style.display = "flex";
    toast.style.alignItems = "center";
    toast.style.gap = "8px";
    
    // Choose neon accent color depending on severity
    let accentColor = "var(--primary)";
    let iconClass = "fa-solid fa-circle-info";
    if (type === "success") {
        accentColor = "var(--success)";
        iconClass = "fa-solid fa-circle-check";
    } else if (type === "warning") {
        accentColor = "var(--warning)";
        iconClass = "fa-solid fa-triangle-exclamation";
    } else if (type === "danger") {
        accentColor = "var(--danger)";
        iconClass = "fa-solid fa-circle-exclamation";
    }
    
    toast.style.borderLeft = `4px solid ${accentColor}`;
    toast.innerHTML = `<i class="${iconClass}" style="color:${accentColor}"></i> <span>${message}</span>`;
    
    container.appendChild(toast);
    
    // Trigger animation
    setTimeout(() => {
        toast.style.opacity = "1";
        toast.style.transform = "translateY(0)";
    }, 50);
    
    // Auto-remove
    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateY(-10px)";
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ================= VIEW & TAB ROUTING SYSTEM =================
function showView(viewId) {
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    const targetView = document.getElementById(viewId);
    if (targetView) {
        targetView.classList.add('active');
    }
    
    if (viewId === 'app-view') {
        switchTab('home');
    }
}

function switchTab(tabId) {
    // Hide all tabs
    document.querySelectorAll('.app-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.querySelectorAll('.bottom-nav-item').forEach(bn => bn.classList.remove('active'));
    
    // Show selected tab
    const targetTab = document.getElementById(`tab-${tabId}`);
    if (targetTab) {
        targetTab.classList.add('active');
    }
    
    // Highlight nav links
    document.querySelectorAll(`.nav-item[data-tab="${tabId}"]`).forEach(n => n.classList.add('active'));
    document.querySelectorAll(`.bottom-nav-item[data-tab="${tabId}"]`).forEach(bn => bn.classList.add('active'));
    
    // Action hooks for specific tabs
    if (tabId === 'history') {
        loadHistory();
    } else if (tabId === 'specialists') {
        detectUserLocation();
    } else if (tabId === 'profile') {
        loadUserProfile();
    } else if (tabId === 'home') {
        loadDashboardStats();
    }
}

// Onboarding slider management
function setSlide(index) {
    onboardingIndex = index;
    const slides = document.querySelectorAll('.onboarding-slider .slide');
    const dots = document.querySelectorAll('.slider-dots .dot');
    
    slides.forEach((s, idx) => {
        if (idx === index) {
            s.classList.add('active');
        } else {
            s.classList.remove('active');
        }
    });
    
    dots.forEach((d, idx) => {
        if (idx === index) {
            d.classList.add('active');
        } else {
            d.classList.remove('active');
        }
    });
}

// Automatic advance slider
setInterval(() => {
    const slides = document.querySelectorAll('.onboarding-slider .slide');
    if (slides.length > 0) {
        let next = (onboardingIndex + 1) % slides.length;
        setSlide(next);
    }
}, 5000);

// Forgot Password toggle step
function showResetStep(step) {
    const step1 = document.getElementById('forgot-step-1');
    const step2 = document.getElementById('forgot-step-2');
    if (step === 1) {
        step1.classList.remove('hidden');
        step2.classList.add('hidden');
    } else {
        step1.classList.add('hidden');
        step2.classList.remove('hidden');
    }
}

// ================= SESSION & AUTHENTICATION =================
async function initSessionState() {
    const savedLang = localStorage.getItem('userLanguage') || 'en';
    setLanguage(savedLang);

    // Check if backend is reachable
    try {
        const check = await fetch(`${API_BASE_URL}/help`);
        if (check.ok) {
            document.getElementById('connection-indicator').className = "connection-status online";
            document.getElementById('connection-indicator').querySelector('.status-text').textContent = "Backend Connected";
        }
    } catch (e) {
        document.getElementById('connection-indicator').className = "connection-status offline";
        document.getElementById('connection-indicator').querySelector('.status-text').textContent = "Server Offline";
    }

    if (currentToken) {
        await fetchUserInfo();
    } else {
        showView('welcome-view');
        setSlide(0);
    }
}

async function fetchUserInfo() {
    try {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });
        
        if (response.ok) {
            currentUser = await response.json();
            
            // Render user profile details across views
            document.getElementById('greeting-user').textContent = currentUser.full_name.split(' ')[0];
            document.getElementById('sidebar-username').textContent = currentUser.full_name;
            document.getElementById('sidebar-email').textContent = currentUser.email;
            
            document.getElementById('profile-fullname').textContent = currentUser.full_name;
            document.getElementById('profile-email-sub').textContent = currentUser.email;
            
            // Edit details inputs
            document.getElementById('edit-name').value = currentUser.full_name;
            document.getElementById('edit-email').value = currentUser.email;
            
            // Resolve profile avatar
            let avatarSrc = "placeholder_avatar.png";
            if (currentUser.avatar_url) {
                avatarSrc = `${API_BASE_URL}/${currentUser.avatar_url}`;
            } else {
                // Generate a custom SVG path data representing a cute user avatar
                avatarSrc = `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%23626d88"><circle cx="12" cy="8" r="4"/><path d="M12 14c-6.1 0-8 4-8 4v2h16v-2s-1.9-4-8-4z"/></svg>`;
            }
            
            document.getElementById('sidebar-avatar').src = avatarSrc;
            document.getElementById('header-avatar').src = avatarSrc;
            document.getElementById('profile-avatar').src = avatarSrc;
            
            showView('app-view');
            await loadDashboardStats();
        } else {
            handleLogout();
        }
    } catch (error) {
        console.error("Authentication fetch user profile failed", error);
        showToast("Backend Server Offline. Check terminal.", "danger");
        handleLogout();
    }
}

function handleLogout() {
    currentToken = null;
    currentUser = null;
    allScans = [];
    localStorage.removeItem("token");
    resetScanState();
    showView('login-view');
    showToast("Session terminated", "info");
}

// Form Handlers
async function handleLogin(event) {
    event.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value.trim();
    
    const params = new URLSearchParams();
    params.append('username', email);
    params.append('password', password);
    
    try {
        const response = await fetch(`${API_BASE_URL}/token`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        });
        
        if (response.ok) {
            const data = await response.json();
            currentToken = data.access_token;
            localStorage.setItem("token", currentToken);
            showToast("Login Successful!", "success");
            await fetchUserInfo();
        } else {
            const err = await response.json();
            showToast(err.detail || "Incorrect email or password", "danger");
        }
    } catch (e) {
        showToast("Connection to backend server failed", "danger");
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const name = document.getElementById('reg-name').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value.trim();
    
    try {
        const response = await fetch(`${API_BASE_URL}/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password, full_name: name })
        });
        
        if (response.ok) {
            showToast("Account created! Please Sign In.", "success");
            showView('login-view');
            document.getElementById('login-email').value = email;
        } else {
            const err = await response.json();
            showToast(err.detail || "Registration failed. Try again.", "danger");
        }
    } catch (e) {
        showToast("Connection failed", "danger");
    }
}

let resetEmail = "";
async function handleForgotPassword(event) {
    event.preventDefault();
    resetEmail = document.getElementById('forgot-email').value.trim();
    
    try {
        const response = await fetch(`${API_BASE_URL}/reset-password`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: resetEmail })
        });
        
        if (response.ok) {
            showToast("Reset code generated! Check terminal logs.", "success");
            showResetStep(2);
        } else {
            const err = await response.json();
            showToast(err.detail || "Email address not found", "danger");
        }
    } catch (e) {
        showToast("Connection error", "danger");
    }
}

async function handleResetPassword(event) {
    event.preventDefault();
    const code = document.getElementById('reset-code').value.trim();
    const newPassword = document.getElementById('new-password').value.trim();
    
    try {
        const response = await fetch(`${API_BASE_URL}/new-password`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: resetEmail, code: code, new_password: newPassword })
        });
        
        if (response.ok) {
            showToast("Password updated successfully!", "success");
            showView('login-view');
            document.getElementById('login-password').value = "";
        } else {
            const err = await response.json();
            showToast(err.detail || "Verification failed. Check code.", "danger");
        }
    } catch (e) {
        showToast("Connection error", "danger");
    }
}

// ================= DASHBOARD & STATISTICS =================
async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/history`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        
        if (response.ok) {
            allScans = await response.json();
            
            // Update logged scan metric
            const totalScansElem = document.getElementById('stat-scans-logged');
            const profileScansElem = document.getElementById('profile-stat-scans');
            if (totalScansElem) totalScansElem.textContent = allScans.length;
            if (profileScansElem) profileScansElem.textContent = allScans.length;
            
            // Calculate health score matching Android App formula (filter valid skin scans)
            const validScans = allScans.filter(s => s.result_title !== "Non-Skin Image" && s.confidence_score > 0);
            
            let healthPercent = 100;
            if (validScans.length > 0) {
                const totalConfidence = validScans.reduce((sum, s) => sum + s.confidence_score, 0);
                const avgConfidence = totalConfidence / validScans.length;
                healthPercent = Math.round(avgConfidence * 0.9);
            }
            
            const profileHealthElem = document.getElementById('profile-stat-health');
            if (profileHealthElem) {
                profileHealthElem.textContent = `${healthPercent}%`;
            }
        }
    } catch (e) {
        console.error("Dashboard stats query error", e);
    }
}

// ================= WEBCAM INTEGRATION =================
async function openWebcam() {
    const video = document.getElementById('webcam-video');
    const modal = document.getElementById('webcam-modal');
    
    try {
        webcamStream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: "environment" }
        });
        video.srcObject = webcamStream;
        modal.classList.remove('hidden');
    } catch (err) {
        console.error("Camera access failed", err);
        showToast("Webcam input access blocked or not found", "danger");
    }
}

function closeWebcam() {
    if (webcamStream) {
        webcamStream.getTracks().forEach(track => track.stop());
        webcamStream = null;
    }
    document.getElementById('webcam-modal').classList.add('hidden');
}

function captureWebcamFrame() {
    const video = document.getElementById('webcam-video');
    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    
    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    
    canvas.toBlob((blob) => {
        if (blob) {
            const file = new File([blob], "webcam_capture.jpg", { type: "image/jpeg" });
            selectedFile = file;
            
            // Set image preview
            const previewImg = document.getElementById('scan-image-preview');
            previewImg.src = URL.createObjectURL(blob);
            
            document.getElementById('scan-init-state').classList.add('hidden');
            document.getElementById('scan-preview-state').classList.remove('hidden');
            closeWebcam();
        }
    }, 'image/jpeg');
}

// ================= SCANNER FLOW =================
function triggerFileInput(id) {
    document.getElementById(id).click();
}

function handleFileSelect(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    if (!file.type.startsWith("image/")) {
        showToast("Please upload a valid skin concern image", "warning");
        return;
    }
    
    selectedFile = file;
    
    // Set preview
    const previewImg = document.getElementById('scan-image-preview');
    previewImg.src = URL.createObjectURL(file);
    
    // If selected from dashboard quick upload, switch tab automatically
    const isQuickInput = event.target.id === 'quick-file-input';
    if (isQuickInput) {
        switchTab('scan');
    }
    
    document.getElementById('scan-init-state').classList.add('hidden');
    document.getElementById('scan-preview-state').classList.remove('hidden');
}

function resetScanState() {
    selectedFile = null;
    document.getElementById('scan-file-input').value = "";
    document.getElementById('quick-file-input').value = "";
    document.getElementById('scan-image-preview').src = "";
    
    document.getElementById('scan-init-state').classList.remove('hidden');
    document.getElementById('scan-preview-state').classList.add('hidden');
    document.getElementById('scan-loading-state').classList.add('hidden');
    document.getElementById('scan-result-state').classList.add('hidden');
}

async function runAnalysis() {
    if (!selectedFile) return;
    
    const init = document.getElementById('scan-preview-state');
    const loading = document.getElementById('scan-loading-state');
    const loaderStatus = document.getElementById('loader-status');
    
    init.classList.add('hidden');
    loading.classList.remove('hidden');
    
    // Simulate steps text
    const steps = [
        "Aligning focus pixels...",
        "Querying CNN layers...",
        "Analyzing texture mapping...",
        "Confirming disease parameters...",
        "Structuring report details..."
    ];
    let stepIdx = 0;
    loaderStatus.textContent = steps[0];
    const stepsInterval = setInterval(() => {
        stepIdx = (stepIdx + 1) % steps.length;
        loaderStatus.textContent = steps[stepIdx];
    }, 850);
    
    // Fetch analyze API
    const formData = new FormData();
    formData.append('file', selectedFile);
    
    try {
        const response = await fetch(`${API_BASE_URL}/analyze`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${currentToken}` },
            body: formData
        });
        
        clearInterval(stepsInterval);
        
        if (response.ok) {
            const data = await response.json();
            renderAnalysisResults(data);
            
            loading.classList.add('hidden');
            document.getElementById('scan-result-state').classList.remove('hidden');
            showToast("Neural scan completed successfully!", "success");
            loadDashboardStats(); // Refresh stats
        } else {
            const err = await response.json();
            showToast(err.detail || "Analysis query failed.", "danger");
            resetScanState();
        }
    } catch (e) {
        clearInterval(stepsInterval);
        showToast("Connection to backend server lost", "danger");
        resetScanState();
    }
}

function renderAnalysisResults(data) {
    // Show image
    let imgUrl = data.image_url;
    if (imgUrl.startsWith("uploads/")) {
        imgUrl = `${API_BASE_URL}/${imgUrl}`;
    }
    document.getElementById('result-skin-img').src = imgUrl;
    
    // Condition title
    document.getElementById('result-condition-title').textContent = data.result_title;
    
    // Circular Confidence Ring
    const scoreVal = data.confidence_score;
    document.getElementById('result-score-val').textContent = `${scoreVal}%`;
    const ring = document.getElementById('score-ring');
    // Circumference = 2 * pi * 50 = 314
    const offset = 314 - (scoreVal / 100) * 314;
    ring.style.strokeDashoffset = offset;
    
    // Description text
    document.getElementById('result-description').innerHTML = data.result_description.replace(/\n/g, '<br>');
    
    // Format Precautions & First Aid
    renderBulletList('result-precautions', data.precautions);
    renderBulletList('result-firstaid', data.first_aid);

    // Show/hide specialist recommendation if condition is a disease
    const recBox = document.getElementById('result-specialist-recommendation');
    if (recBox) {
        if (data.result_title !== "Normal Healthy Skin" && data.result_title !== "Non-Skin Image") {
            recBox.classList.remove('hidden');
        } else {
            recBox.classList.add('hidden');
        }
    }
}

function navigateToSpecialists() {
    switchTab('specialists');
    resetScanState();
}

function renderBulletList(elementId, text) {
    const list = document.getElementById(elementId);
    list.innerHTML = "";
    if (!text) {
        list.innerHTML = "<li>No details available.</li>";
        return;
    }
    
    const items = text.split('|');
    items.forEach(item => {
        if (item.trim()) {
            const li = document.createElement('li');
            li.textContent = item.trim();
            list.appendChild(li);
        }
    });
}

// ================= HISTORY LOGS ARCHIVES =================
async function loadHistory() {
    try {
        const response = await fetch(`${API_BASE_URL}/history`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        
        if (response.ok) {
            allScans = await response.json();
            filterHistory(); // Render and search
        }
    } catch (e) {
        showToast("Failed to fetch archives", "danger");
    }
}

function filterHistory() {
    const query = document.getElementById('history-search').value.toLowerCase().trim();
    const container = document.getElementById('history-list-container');
    container.innerHTML = "";
    
    const filtered = allScans.filter(scan => 
        scan.result_title.toLowerCase().includes(query) ||
        scan.result_description.toLowerCase().includes(query)
    );
    
    if (filtered.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fa-solid fa-notes-medical" style="font-size:3rem;color:var(--text-muted);margin-bottom:12px;"></i>
                <p>No scans logged in archives match your query</p>
            </div>
        `;
        return;
    }
    
    // Sort descending by date
    filtered.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));
    
    filtered.forEach(scan => {
        const dateStr = new Date(scan.created_at).toLocaleString();
        let imgUrl = scan.image_url;
        if (imgUrl.startsWith("uploads/")) {
            imgUrl = `${API_BASE_URL}/${imgUrl}`;
        }
        
        const isHealthy = scan.result_title === 'Normal Healthy Skin';
        const badgeClass = isHealthy ? 'healthy' : 'danger';
        
        const item = document.createElement('div');
        item.className = 'history-item';
        item.innerHTML = `
            <div class="history-info-left">
                <img src="${imgUrl}" class="history-img-thumb">
                <div class="history-details-text">
                    <h4>${scan.result_title}</h4>
                    <p><i class="fa-regular fa-calendar"></i> ${dateStr}</p>
                </div>
            </div>
            <div class="history-info-right">
                <span class="history-score-badge ${badgeClass}">${scan.confidence_score}%</span>
                <button class="btn-table-action btn-view" title="View details"><i class="fa-solid fa-eye"></i></button>
                <button class="btn-table-action btn-delete" title="Delete log"><i class="fa-solid fa-trash-can" style="color:var(--danger)"></i></button>
            </div>
        `;
        
        // Clicks
        item.querySelector('.btn-view').onclick = () => openHistoryModal(scan);
        item.querySelector('.btn-delete').onclick = () => deleteHistoryItem(scan.id);
        
        container.appendChild(item);
    });
}

// History Detail Modal details
let activeHistoryId = null;

function openHistoryModal(scan) {
    activeHistoryId = scan.id;
    const modal = document.getElementById('history-modal');
    const content = document.getElementById('history-modal-content');
    
    let imgUrl = scan.image_url;
    if (imgUrl.startsWith("uploads/")) {
        imgUrl = `${API_BASE_URL}/${imgUrl}`;
    }
    
    const isHealthy = scan.result_title === 'Normal Healthy Skin';
    const badgeText = isHealthy ? 'Healthy Skin Checked' : 'AI Disease Indication';
    const badgeColor = isHealthy ? 'var(--success)' : 'var(--danger)';
    
    content.innerHTML = `
        <div class="result-layout">
            <div class="result-hero">
                <div class="result-image-holder">
                    <img src="${imgUrl}">
                </div>
                <div class="result-score-container">
                    <div style="text-align:center;">
                        <h2 style="color:${badgeColor};font-family:var(--font-heading);">${scan.confidence_score}%</h2>
                        <span style="font-size:0.75rem;color:var(--text-secondary);text-transform:uppercase;">Model Confidence</span>
                    </div>
                </div>
            </div>
            <div class="result-details">
                <div class="result-badge" style="background:rgba(255,255,255,0.02);border-color:${badgeColor};color:${badgeColor};">${badgeText}</div>
                <div class="detail-section">
                    <h4><i class="fa-solid fa-circle-info"></i> Pathology Details</h4>
                    <p style="white-space:pre-line;">${scan.result_description}</p>
                </div>
                <div class="detail-section warning">
                    <h4><i class="fa-solid fa-triangle-exclamation"></i> Precautions</h4>
                    <p style="white-space:pre-line;">${scan.precautions ? scan.precautions.replace(/\|/g, '\n') : 'No details.'}</p>
                </div>
                <div class="detail-section info">
                    <h4><i class="fa-solid fa-kit-medical"></i> First Aid Protocol</h4>
                    <p style="white-space:pre-line;">${scan.first_aid ? scan.first_aid.replace(/\|/g, '\n') : 'No details.'}</p>
                </div>
                ${(!isHealthy && scan.result_title !== 'Non-Skin Image') ? `
                <div class="detail-section recommendation">
                    <h4><i class="fa-solid fa-user-doctor"></i> Recommended Action</h4>
                    <p>Based on this scan, we recommend consulting a dermatologist. Find verified clinics within 100km.</p>
                    <button class="btn btn-secondary mt-12 btn-block" onclick="navigateToSpecialistsFromModal()"><i class="fa-solid fa-map-location-dot"></i> View Nearby Specialists</button>
                </div>
                ` : ''}
            </div>
        </div>
    `;
    
    modal.classList.remove('hidden');
}

function navigateToSpecialistsFromModal() {
    closeHistoryModal();
    switchTab('specialists');
}

function closeHistoryModal() {
    document.getElementById('history-modal').classList.add('hidden');
    activeHistoryId = null;
}

async function deleteHistoryItem(id) {
    if (!confirm("Are you sure you want to permanently delete this diagnostic scan log?")) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/history/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        
        if (response.ok) {
            showToast("Scan record deleted", "success");
            await loadHistory();
        } else {
            showToast("Failed to delete record", "danger");
        }
    } catch (e) {
        showToast("Connection failed", "danger");
    }
}

async function deleteHistoryItemFromModal() {
    if (activeHistoryId) {
        await deleteHistoryItem(activeHistoryId);
        closeHistoryModal();
    }
}

// ================= CLINICS & GEOLOCATION RADAR =================
function detectUserLocation() {
    const statusText = document.getElementById('radar-location-status');
    const coordText = document.getElementById('radar-coordinates');
    
    if (navigator.geolocation) {
        statusText.textContent = "Acquiring coordinates...";
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                localCoords.lat = pos.coords.latitude;
                localCoords.lon = pos.coords.longitude;
                statusText.textContent = "Position Identified";
                coordText.textContent = `${localCoords.lat.toFixed(4)}, ${localCoords.lon.toFixed(4)}`;
                
                fetchNearbySpecialists();
            },
            (err) => {
                console.warn("Geolocation blocked", err);
                statusText.textContent = "Using Chennai Coordinates (Mocked)";
                coordText.textContent = `${localCoords.lat.toFixed(4)}, ${localCoords.lon.toFixed(4)}`;
                fetchNearbySpecialists();
            }
        );
    } else {
        statusText.textContent = "Location Service Unsupported";
        coordText.textContent = `${localCoords.lat.toFixed(4)}, ${localCoords.lon.toFixed(4)}`;
        fetchNearbySpecialists();
    }
}

function refreshSpecialistRadar() {
    detectUserLocation();
}

async function fetchNearbySpecialists() {
    const container = document.getElementById('clinics-list-container');
    container.innerHTML = `
        <div class="loading-clinics">
            <i class="fa-solid fa-circle-notch fa-spin"></i>
            <p>Scanning nodes within 100km...</p>
        </div>
    `;
    
    try {
        const response = await fetch(`${API_BASE_URL}/doctors?lat=${localCoords.lat}&lon=${localCoords.lon}`);
        if (response.ok) {
            const doctors = await response.json();
            renderClinics(doctors);
        } else {
            container.innerHTML = `<div class="loading-clinics"><p style="color:var(--danger)">API request failed</p></div>`;
        }
    } catch (e) {
        container.innerHTML = `<div class="loading-clinics"><p style="color:var(--danger)">Connection to specialists server failed</p></div>`;
    }
}

function renderClinics(doctors) {
    const container = document.getElementById('clinics-list-container');
    container.innerHTML = "";
    
    if (doctors.length === 0) {
        container.innerHTML = `
            <div class="loading-clinics">
                <i class="fa-solid fa-map-location" style="font-size:2rem;color:var(--text-muted);"></i>
                <p>No skincare specialists found within 100km</p>
            </div>
        `;
        return;
    }
    
    doctors.forEach(doc => {
        const card = document.createElement('div');
        card.className = 'clinic-card';
        card.innerHTML = `
            <div class="clinic-info-left">
                <h4>${doc.name}</h4>
                <span class="clinic-specialty">${doc.specialty}</span>
                <span class="clinic-address" title="${doc.address}"><i class="fa-solid fa-location-dot"></i> ${doc.address}</span>
            </div>
            <div class="clinic-info-right">
                <span class="clinic-rating"><i class="fa-solid fa-star"></i> ${doc.rating}</span>
                <span class="clinic-distance">${doc.distance}</span>
            </div>
        `;
        
        card.onclick = () => selectClinic(doc, card);
        container.appendChild(card);
    });
}

function selectClinic(doc, element) {
    document.querySelectorAll('.clinic-card').forEach(c => c.classList.remove('selected'));
    element.classList.add('selected');
    
    document.getElementById('map-target-name').textContent = doc.name;
    document.getElementById('map-target-address').textContent = `${doc.specialty} • ${doc.address}`;
    
    const routeBtn = document.getElementById('btn-open-navigation');
    routeBtn.classList.remove('hidden');
    routeBtn.href = `https://www.google.com/maps/dir/?api=1&destination=${doc.latitude},${doc.longitude}`;
}

// ================= PROFILE MANAGEMENT & AVATAR SYNC =================
async function loadUserProfile() {
    if (!currentUser) return;
    document.getElementById('edit-name').value = currentUser.full_name;
    document.getElementById('edit-email').value = currentUser.email;
    await loadDashboardStats();
}

function triggerAvatarUpload() {
    document.getElementById('avatar-file-input').click();
}

async function handleAvatarUpload(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    if (!file.type.startsWith("image/")) {
        showToast("Please upload an image file format", "warning");
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    
    showToast("Synchronizing avatar to database...", "info");
    
    try {
        const response = await fetch(`${API_BASE_URL}/users/me/avatar`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${currentToken}` },
            body: formData
        });
        
        if (response.ok) {
            showToast("Profile image updated successfully!", "success");
            await fetchUserInfo();
        } else {
            showToast("Failed to upload image", "danger");
        }
    } catch (e) {
        showToast("Connection to server failed", "danger");
    }
}

async function handleProfileUpdate(event) {
    event.preventDefault();
    const name = document.getElementById('edit-name').value.trim();
    const email = document.getElementById('edit-email').value.trim();
    
    try {
        const response = await fetch(`${API_BASE_URL}/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ full_name: name, email: email })
        });
        
        if (response.ok) {
            showToast("Profile details updated successfully!", "success");
            await fetchUserInfo();
        } else {
            const err = await response.json();
            showToast(err.detail || "Failed to update profile", "danger");
        }
    } catch (e) {
        showToast("Connection failed", "danger");
    }
}

// ================= MULTI-LANGUAGE TRANSLATION SYSTEM =================
const i18n = {
    en: {
        nav_dashboard: "Dashboard",
        nav_scan: "AI Scan",
        nav_history: "Scans History",
        nav_specialists: "Specialists",
        nav_profile: "Profile Settings",
        
        banner_tag: "NEURAL DIAGNOSTICS",
        banner_title: "Instant AI Skin Analysis",
        banner_desc: "Capture or upload an image of any skin concern. The convolutional neural network will diagnose conditions and output clinical protocols instantly.",
        btn_init_scanner: "Initialize Scanner",
        
        stat_scans_logged: "Scans Logged",
        stat_system_accuracy: "System Accuracy",
        stat_high: "High",
        
        quick_upload_title: "Quick Upload",
        quick_upload_desc: "Drag and drop or select an image directly to analyze.",
        quick_drop_text: "Drag files here or browse archives",
        
        insights_title: "Neural Insights",
        insights_text: "UV radiation index is active. Always wear SPF-30+ protection and perform skin hydration checks.",
        
        profile_scans: "Total Scans",
        profile_health: "Health Score",
        profile_plan: "Plan Type",
        lang_preferences: "Language Preferences",
        lang_desc: "Select translation target for diagnosis reports"
    },
    hi: {
        nav_dashboard: "डैशबोर्ड",
        nav_scan: "एआई स्कैन",
        nav_history: "स्कैन इतिहास",
        nav_specialists: "विशेषज्ञ",
        nav_profile: "प्रोफाइल सेटिंग्स",
        
        banner_tag: "तंत्रिका निदान",
        banner_title: "त्वरित एआई त्वचा विश्लेषण",
        banner_desc: "किसी भी त्वचा समस्या की तस्वीर लें या अपलोड करें। तंत्रिका नेटवर्क तुरंत स्थितियों का निदान करेगा और नैदानिक प्रोटोकॉल प्रदान करेगा।",
        btn_init_scanner: "स्कैनर प्रारंभ करें",
        
        stat_scans_logged: "स्कैन लॉग किए गए",
        stat_system_accuracy: "सिस्टम सटीकता",
        stat_high: "उच्च",
        
        quick_upload_title: "त्वरित अपलोड",
        quick_upload_desc: "विश्लेषण के लिए सीधे छवि खींचें और छोड़ें या चुनें।",
        quick_drop_text: "फाइलें यहां खींचें या डिजिटल गैलरी से चुनें",
        
        insights_title: "तंत्रिका अंतर्दृष्टि",
        insights_text: "दैनिक यूवी सूचकांक चरम पर हैं। सनस्क्रीन SPF-30+ सुरक्षा प्रोटोकॉल सक्रिय रखें।",
        
        profile_scans: "कुल स्कैन",
        profile_health: "स्वास्थ्य स्कोर",
        profile_plan: "प्लान प्रकार",
        lang_preferences: "भाषा प्राथमिकताएं",
        lang_desc: "निदान रिपोर्ट के लिए अनुवाद भाषा चुनें"
    },
    te: {
        nav_dashboard: "డాష్‌బోర్డ్",
        nav_scan: "AI స్కాన్",
        nav_history: "స్కాన్ల చరిత్ర",
        nav_specialists: "నిపుణులు",
        nav_profile: "ప్రొఫైల్ సెట్టింగ్స్",
        
        banner_tag: "న్యూరల్ డయాగ్నోస్టిక్స్",
        banner_title: "తక్షణ AI చర్మ విశ్లేషణ",
        banner_desc: "ఏదైనా చర్మ సమస్య యొక్క చిత్రాన్ని క్యాప్చర్ చేయండి లేదా అప్‌లోడ్ చేయండి. న్యూరల్ నెట్‌వర్క్ పరిస్థితులను విశ్లేషిస్తుంది.",
        btn_init_scanner: "స్కానర్‌ను ప్రారంభించండి",
        
        stat_scans_logged: "నమోదైన స్కాన్‌లు",
        stat_system_accuracy: "సిస్టమ్ ఖచ్చితత్వం",
        stat_high: "అధికం",
        
        quick_upload_title: "త్వరిత అప్‌లోడ్",
        quick_upload_desc: "విశ్లేషణ కోసం చిత్రాన్ని ఇక్కడకు డ్రాగ్ చేయండి లేదా ఎంచుకోండి.",
        quick_drop_text: "ఫైళ్లను ఇక్కడ వేయండి లేదా గ్యాలరీ నుండి ఎంచుకోండి",
        
        insights_title: "న్యూరల్ అంతర్దృష్టులు",
        insights_text: "రోజువారీ UV సూచికలు గరిష్టంగా ఉన్నాయి. SPF-30+ సూర్యరశ్మి రక్షణను ఉపయోగించండి.",
        
        profile_scans: "మొత్తం స్కాన్‌లు",
        profile_health: "ఆరోగ్య స్కోర్",
        profile_plan: "ప్లాన్ రకం",
        lang_preferences: "భాషా ప్రాధాన్యతలు",
        lang_desc: "నివేదికల కోసం అనువాద లక్ష్యాన్ని ఎంచుకోండి"
    },
    ta: {
        nav_dashboard: "டேஷ்போர்டு",
        nav_scan: "AI ஸ்கேன்",
        nav_history: "ஸ்கேன் வரலாறு",
        nav_specialists: "நிபுணர்கள்",
        nav_profile: "சுயவிவர அமைப்புகள்",
        
        banner_tag: "நரம்பியல் நோயறிதல்",
        banner_title: "உடனடி AI தோல் பகுப்பாய்வு",
        banner_desc: "தோல் பிரச்சனையின் படத்தைப் பிடிக்கவும் அல்லது பதிவேற்றவும். நரம்பியல் நெட்வொர்க் நிலைகளைக் கண்டறியும்.",
        btn_init_scanner: "ஸ்கேனரைத் தொடங்கு",
        
        stat_scans_logged: "பதிவு செய்யப்பட்ட ஸ்கேன்கள்",
        stat_system_accuracy: "துல்லியம்",
        stat_high: "உயர்",
        
        quick_upload_title: "விரைவு பதிவேற்றம்",
        quick_upload_desc: "பகுப்பாய்வு செய்ய படத்தை நேரடியாக இழுத்து விடவும்.",
        quick_drop_text: "கேலரியில் இருந்து தேர்ந்தெடுக்கவும்",
        
        insights_title: "நுண்ணறிவுகள்",
        insights_text: "தினசரி புற ஊதா குறியீடுகள் உச்சத்தில் உள்ளன. SPF-30+ பாதுகாப்பு நெறிமுறைகளைப் பின்பற்றவும்.",
        
        profile_scans: "மொத்த ஸ்கேன்கள்",
        profile_health: "ஆரோக்கிய மதிப்பெண்",
        profile_plan: "திட்ட வகை",
        lang_preferences: "மொழி விருப்பங்கள்",
        lang_desc: "மொழிபெயர்ப்பு இலக்கைத் தேர்ந்தெடுக்கவும்"
    },
    kn: {
        nav_dashboard: "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್",
        nav_scan: "AI ಸ್ಕ್ಯಾನ್",
        nav_history: "ಸ್ಕ್ಯಾನ್ ಇತಿಹಾಸ",
        nav_specialists: "ತಜ್ಞರು",
        nav_profile: "ಪ್ರೊಫೈಲ್ ಸೆಟ್ಟಿಂಗ್‌ಗಳು",
        
        banner_tag: "ನ್ಯೂರಲ್ ರೋಗನಿರ್ಣಯ",
        banner_title: "ತ್ವರಿತ AI ಚರ್ಮದ ವಿಶ್ಲೇಷಣೆ",
        banner_desc: "ಯಾವುದೇ ಚರ್ಮದ ಸಮಸ್ಯೆಯ ಚಿತ್ರವನ್ನು ಸೆರೆಹಿಡಿಯಿರಿ ಅಥವಾ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ. ನ್ಯೂರಲ್ ನೆಟ್‌ವರ್ಕ್ ಪರಿಸ್ಥಿತಿಗಳನ್ನು ವಿಶ್ಲೇಷಿಸುತ್ತದೆ.",
        btn_init_scanner: "ಸ್ಕ್ಯಾನರ್ ಪ್ರಾರಂಭಿಸಿ",
        
        stat_scans_logged: "ದಾಖಲಾದ ಸ್ಕ್ಯಾನ್‌ಗಳು",
        stat_system_accuracy: "ಸಿಸ್ಟಮ್ ನಿಖರತೆ",
        stat_high: "ಉನ್ನತ",
        
        quick_upload_title: "ತ್ವರಿತ ಅಪ್‌ಲೋಡ್",
        quick_upload_desc: "ವಿಶ್ಲೇಷಿಸಲು ಚಿತ್ರವನ್ನು ಇಲ್ಲಿಗೆ ಎಳೆಯಿರಿ ಅಥವಾ ಆಯ್ಕೆಮಾಡಿ.",
        quick_drop_text: "ಗ್ಯಾಲರಿಯಿಂದ ಆಯ್ಕೆಮಾಡಿ",
        
        insights_title: "ಒಳನೋಟಗಳು",
        insights_text: "ದೈನಂದಿನ ಯುವಿ ಸೂಚ್ಯಂಕಗಳು ಗರಿಷ್ಠ ಮಟ್ಟದಲ್ಲಿವೆ. SPF-30+ ರಕ್ಷಣೆ ಸಕ್ರಿಯಗೊಳಿಸಿ.",
        
        profile_scans: "ಒಟ್ಟು ಸ್ಕಾನ್‌ಗಳು",
        profile_health: "ಆರೋಗ್ಯ ಸ್ಕೋರ್",
        profile_plan: "ಪ್ಲಾನ್ ಪ್ರಕಾರ",
        lang_preferences: "ಭಾಷಾ ಆದ್ಯತೆಗಳು",
        lang_desc: "ಅನುವಾದ ಭಾಷೆಯನ್ನು ಆಯ್ಕೆಮಾಡಿ"
    },
    ml: {
        nav_dashboard: "ഡാഷ്‌ബോർഡ്",
        nav_scan: "AI സ്കാൻ",
        nav_history: "സ്കാൻ ചരിത്രം",
        nav_specialists: "വിദഗ്ദ്ധർ",
        nav_profile: "പ്രൊഫൈൽ ക്രമീകരണങ്ങൾ",
        
        banner_tag: "ന്യൂറൽ ഡയഗ്നോസ്റ്റിക്സ്",
        banner_title: "ഉടനടി AI ചർമ്മ വിശകലനം",
        banner_desc: "ചർമ്മ പ്രശ്നത്തിന്റെ ചിത്രം ക്യാപ്ചർ ചെയ്യുക അല്ലെങ്കിൽ അപ്‌ലോഡ് ചെയ്യുക. ന്യൂറൽ നെറ്റ്‌വർക്ക് വിശകലനം ചെയ്യും.",
        btn_init_scanner: "സ്കാൻ ആരംഭിക്കുക",
        
        stat_scans_logged: "ലോഗ് ചെയ്ത സ്കാനുകൾ",
        stat_system_accuracy: "സിസ്റ്റം കൃത്യത",
        stat_high: "ഉയർന്നത്",
        
        quick_upload_title: "ക്വിക്ക് അപ്‌ലോഡ്",
        quick_upload_desc: "വിശകലനം ചെയ്യാൻ ചിത്രം നേരിട്ട് തിരഞ്ഞെടുക്കുക.",
        quick_drop_text: "ഗാലറിയിൽ നിന്ന് തിരഞ്ഞെടുക്കുക",
        
        insights_title: "ന്യൂറൽ ഉൾക്കാഴ്ചകൾ",
        insights_text: "അൾട്രാവയലറ്റ് സൂചികകൾ ഉയർന്നതാണ്. SPF-30+ സൂര്യപ്രകാശ സംരക്ഷണം ഉപയോഗിക്കുക.",
        
        profile_scans: "ആകെ സ്കാനുകൾ",
        profile_health: "ആരോഗ്യ സ്കോർ",
        profile_plan: "പ്ലാൻ ടൈപ്പ്",
        lang_preferences: "ഭാഷാ മുൻഗണനകൾ",
        lang_desc: "തർജ്ജമ ഭാഷ തിരഞ്ഞെടുക്കുക"
    }
};

function applyLanguage(lang) {
    currentLanguage = lang || 'en';
    const dict = i18n[currentLanguage] || i18n['en'];
    
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (dict[key]) {
            el.textContent = dict[key];
        }
    });
    
    localStorage.setItem('userLanguage', currentLanguage);
}

function setLanguage(lang) {
    currentLanguage = lang;
    document.querySelectorAll('.lang-btn').forEach(btn => btn.classList.remove('active'));
    const targetBtn = document.getElementById(`lang-${lang}`);
    if (targetBtn) targetBtn.classList.add('active');
    
    applyLanguage(lang);
    showToast(`Translation language set to ${lang.toUpperCase()}`, "info");
}

// ================= DOM LOAD INITIALIZATION =================
window.addEventListener("DOMContentLoaded", () => {
    initSessionState();
});
