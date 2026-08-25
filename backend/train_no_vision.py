import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
import os
import json
from PIL import Image
import numpy as np

# Configuration
DATA_DIR = r"c:\DermaCareAI_New\backend\dataset"
TRAIN_DIR = os.path.join(DATA_DIR, "train")
VAL_DIR = os.path.join(DATA_DIR, "test")
MODEL_SAVE_PATH = r"c:\DermaCareAI_New\backend\skin_model.pth"
LABELS_SAVE_PATH = r"c:\DermaCareAI_New\backend\labels.json"
BATCH_SIZE = 32
EPOCHS = 10  # Increased for better accuracy
LEARNING_RATE = 0.0005

class SkinDataset(Dataset):
    def __init__(self, root_dir):
        print(f"Indexing {root_dir}...")
        self.root_dir = root_dir
        self.classes = sorted([d for d in os.listdir(root_dir) if os.path.isdir(os.path.join(root_dir, d))])
        self.image_paths = []
        self.labels = []
        
        for i, cls in enumerate(self.classes):
            cls_dir = os.path.join(root_dir, cls)
            for img_name in os.listdir(cls_dir):
                if img_name.lower().endswith(('.png', '.jpg', '.jpeg')):
                    self.image_paths.append(os.path.join(cls_dir, img_name))
                    self.labels.append(i)
        print(f"Found {len(self.image_paths)} images across {len(self.classes)} classes.")
        
    def __len__(self):
        return len(self.image_paths)
    
    def __getitem__(self, idx):
        try:
            img_path = self.image_paths[idx]
            image = Image.open(img_path).convert('RGB')
            image = image.resize((128, 128))
            image = np.array(image).transpose((2, 0, 1)) / 255.0
            label = self.labels[idx]
            return torch.tensor(image, dtype=torch.float32), torch.tensor(label, dtype=torch.long)
        except Exception as e:
            # Return a blank image if one fails
            return torch.zeros((3, 128, 128)), torch.tensor(0)

class AccurateCNN(nn.Module):
    def __init__(self, num_classes):
        super(AccurateCNN, self).__init__()
        self.features = nn.Sequential(
            # Layer 1
            nn.Conv2d(3, 32, kernel_size=3, padding=1),
            nn.BatchNorm2d(32),
            nn.ReLU(),
            nn.MaxPool2d(2),
            
            # Layer 2
            nn.Conv2d(32, 64, kernel_size=3, padding=1),
            nn.BatchNorm2d(64),
            nn.ReLU(),
            nn.MaxPool2d(2),
            
            # Layer 3
            nn.Conv2d(64, 128, kernel_size=3, padding=1),
            nn.BatchNorm2d(128),
            nn.ReLU(),
            nn.MaxPool2d(2),
            
            # Layer 4
            nn.Conv2d(128, 256, kernel_size=3, padding=1),
            nn.BatchNorm2d(256),
            nn.ReLU(),
            nn.MaxPool2d(2),
            
            nn.Flatten()
        )
        self.classifier = nn.Sequential(
            nn.Linear(256 * 8 * 8, 512),
            nn.ReLU(),
            nn.Dropout(0.5),
            nn.Linear(512, num_classes)
        )
        
    def forward(self, x):
        x = self.features(x)
        x = self.classifier(x)
        return x

def train():
    print("--- STARTING ACCURATE TRAINING ---")
    train_ds = SkinDataset(TRAIN_DIR)
    val_ds = SkinDataset(VAL_DIR)
    
    train_loader = DataLoader(train_ds, batch_size=BATCH_SIZE, shuffle=True)
    val_loader = DataLoader(val_ds, batch_size=BATCH_SIZE)
    
    class_names = train_ds.classes
    with open(LABELS_SAVE_PATH, 'w') as f:
        json.dump(class_names, f)
    
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")
    
    model = AccurateCNN(len(class_names)).to(device)
    
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=LEARNING_RATE)
    
    for epoch in range(EPOCHS):
        model.train()
        running_loss = 0.0
        correct = 0
        total = 0
        
        for i, (images, labels) in enumerate(train_loader):
            images, labels = images.to(device), labels.to(device)
            optimizer.zero_grad()
            outputs = model(images)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()
            
            running_loss += loss.item()
            _, predicted = outputs.max(1)
            total += labels.size(0)
            correct += predicted.eq(labels).sum().item()
            
            if (i+1) % 50 == 0:
                print(f"Epoch {epoch+1}/{EPOCHS} | Batch {i+1}/{len(train_loader)} | Loss: {running_loss/(i+1):.4f} | Acc: {100.*correct/total:.2f}%")
        
        # Validation
        model.eval()
        val_correct = 0
        val_total = 0
        with torch.no_grad():
            for images, labels in val_loader:
                images, labels = images.to(device), labels.to(device)
                outputs = model(images)
                _, predicted = outputs.max(1)
                val_total += labels.size(0)
                val_correct += predicted.eq(labels).sum().item()
        
        print(f"Epoch {epoch+1} Complete. Validation Acc: {100.*val_correct/val_total:.2f}%")
        
        # Save after every epoch for persistence
        torch.save(model.state_dict(), MODEL_SAVE_PATH)
        print(f"Checkpoint saved to {MODEL_SAVE_PATH}")
        
    torch.save(model.state_dict(), MODEL_SAVE_PATH)
    print(f"Final Model saved to {MODEL_SAVE_PATH}")

if __name__ == "__main__":
    train()
