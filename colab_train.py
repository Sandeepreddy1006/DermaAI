import os
import glob
import json
import zipfile
import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import datasets, transforms, models
from torch.utils.data import DataLoader
from google.colab import files

def main():
    print("--- STARTING COLAB MOBILENETV2 SKIN MODEL TRAINING ---")
    
    # 1. Locate and extract dataset zip
    zip_files = glob.glob("/content/*.zip")
    if not zip_files:
        print("❌ Error: No .zip dataset file found in /content/! Please upload your dataset zip file to Colab.")
        return

    zip_path = zip_files[0]
    print(f"📦 Extracting dataset archive: {zip_path}")
    
    extract_dir = "/content/dataset"
    os.makedirs(extract_dir, exist_ok=True)
    with zipfile.ZipFile(zip_path, 'r') as zip_ref:
        zip_ref.extractall(extract_dir)

    # Auto-locate directory containing train and test folders
    found_train = [r for r, d, _ in os.walk(extract_dir) if "train" in d]
    DATA_DIR = found_train[0] if found_train else extract_dir

    TRAIN_DIR = os.path.join(DATA_DIR, "train")
    VAL_DIR = os.path.join(DATA_DIR, "test")
    MODEL_SAVE_PATH = "skin_model.pth"
    LABELS_SAVE_PATH = "labels.json"

    BATCH_SIZE = 32
    EPOCHS = 15
    LEARNING_RATE = 0.0001

    # 2. Augmentation & Data Loaders
    data_transforms = {
        'train': transforms.Compose([
            transforms.RandomResizedCrop(224),
            transforms.RandomHorizontalFlip(),
            transforms.RandomRotation(15),
            transforms.ToTensor(),
            transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
        ]),
        'val': transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
        ]),
    }

    image_datasets = {
        'train': datasets.ImageFolder(TRAIN_DIR, data_transforms['train']),
        'val': datasets.ImageFolder(VAL_DIR, data_transforms['val'])
    }

    dataloaders = {
        'train': DataLoader(image_datasets['train'], batch_size=BATCH_SIZE, shuffle=True, num_workers=0),
        'val': DataLoader(image_datasets['val'], batch_size=BATCH_SIZE, shuffle=False, num_workers=0)
    }

    class_names = image_datasets['train'].classes
    with open(LABELS_SAVE_PATH, 'w') as f:
        json.dump(class_names, f)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"🚀 Training Device: {device} | Total Skin Classes ({len(class_names)}): {class_names}")

    # 3. Initialize MobileNetV2 Architecture
    try:
        from torchvision.models import MobileNet_V2_Weights
        model = models.mobilenet_v2(weights=MobileNet_V2_Weights.DEFAULT)
    except Exception:
        model = models.mobilenet_v2(pretrained=True)

    for param in model.parameters():
        param.requires_grad = True

    num_ftrs = model.classifier[1].in_features
    model.classifier[1] = nn.Sequential(
        nn.Dropout(0.3),
        nn.Linear(num_ftrs, len(class_names))
    )
    model = model.to(device)

    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=LEARNING_RATE)

    # 4. Training Loop
    best_acc = 0.0

    for epoch in range(EPOCHS):
        model.train()
        running_loss, running_corrects = 0.0, 0
        print(f"\nEpoch {epoch+1}/{EPOCHS}")
        
        for i, (inputs, labels) in enumerate(dataloaders['train']):
            inputs, labels = inputs.to(device), labels.to(device)
            optimizer.zero_grad()
            outputs = model(inputs)
            _, preds = torch.max(outputs, 1)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()
            
            running_loss += loss.item() * inputs.size(0)
            running_corrects += torch.sum(preds == labels.data)
            
        epoch_loss = running_loss / len(image_datasets['train'])
        epoch_acc = running_corrects.double() / len(image_datasets['train'])
        print(f"Train Loss: {epoch_loss:.4f} | Train Acc: {epoch_acc*100:.2f}%")
        
        # Validation
        model.eval()
        val_corrects = 0
        with torch.no_grad():
            for inputs, labels in dataloaders['val']:
                inputs, labels = inputs.to(device), labels.to(device)
                outputs = model(inputs)
                _, preds = torch.max(outputs, 1)
                val_corrects += torch.sum(preds == labels.data)
                
        val_acc = val_corrects.double() / len(image_datasets['val'])
        print(f"Validation Acc: {val_acc*100:.2f}%")
        
        if val_acc > best_acc:
            best_acc = val_acc
            torch.save(model.state_dict(), MODEL_SAVE_PATH)
            print(f"⭐ Saved Best Model Checkpoint (Val Acc: {val_acc*100:.2f}%)")

    print(f"\n🎉 Training Finished! Best Accuracy: {best_acc*100:.2f}%")
    print("Downloading skin_model.pth and labels.json...")
    files.download(MODEL_SAVE_PATH)
    files.download(LABELS_SAVE_PATH)

if __name__ == "__main__":
    main()
