import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import datasets, transforms
from torch.utils.data import DataLoader
import os
import json
import ai_model

# Configuration
DATA_DIR = r"c:\DermaCareAI_New\backend\dataset"
TRAIN_DIR = os.path.join(DATA_DIR, "train")
VAL_DIR = os.path.join(DATA_DIR, "test")
MODEL_SAVE_PATH = r"c:\DermaCareAI_New\backend\skin_model.pth"
LABELS_SAVE_PATH = r"c:\DermaCareAI_New\backend\labels.json"
BATCH_SIZE = 64  # Larger batch size to process faster on CPU
EPOCHS = 5
LEARNING_RATE = 0.001

def train():
    print("--- STARTING FAST PROFESSIONAL TRANSFER LEARNING (MobileNetV2 Classifier Only) ---")
    
    # Transforms
    data_transforms = {
        'train': transforms.Compose([
            transforms.RandomResizedCrop(224),
            transforms.RandomHorizontalFlip(),
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

    print("Loading datasets...")
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
    
    print(f"Classes: {len(class_names)}")
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")
    
    # Get model with base frozen (fine_tune=False) to speed up CPU training
    model = ai_model.get_model(len(class_names), fine_tune=False).to(device)
    
    criterion = nn.CrossEntropyLoss()
    # Optimize ONLY classifier head parameters
    optimizer = optim.Adam(model.classifier.parameters(), lr=LEARNING_RATE)
    
    for epoch in range(EPOCHS):
        model.train()
        running_loss = 0.0
        running_corrects = 0
        
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
            
            if (i+1) % 20 == 0:
                print(f"Batch {i+1}/{len(dataloaders['train'])} | Loss: {loss.item():.4f}")
        
        epoch_loss = running_loss / len(image_datasets['train'])
        epoch_acc = running_corrects.double() / len(image_datasets['train'])
        print(f"Train Loss: {epoch_loss:.4f} Acc: {epoch_acc:.4f}")
        
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
        print(f"Validation Acc: {val_acc:.4f}")
        
        # Save checkpoint
        torch.save(model.state_dict(), MODEL_SAVE_PATH)
        print(f"Checkpoint saved to {MODEL_SAVE_PATH}")

    print("\nTraining Complete. Final model saved.")

if __name__ == "__main__":
    train()
