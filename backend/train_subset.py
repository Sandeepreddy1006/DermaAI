import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import datasets, transforms
from torch.utils.data import DataLoader, Subset
import os
import json
import numpy as np
import ai_model

# Configuration
DATA_DIR = r"c:\DermaCareAI_New\backend\dataset"
TRAIN_DIR = os.path.join(DATA_DIR, "train")
VAL_DIR = os.path.join(DATA_DIR, "test")
MODEL_SAVE_PATH = r"c:\DermaCareAI_New\backend\skin_model.pth"
LABELS_SAVE_PATH = r"c:\DermaCareAI_New\backend\labels.json"
BATCH_SIZE = 32
EPOCHS = 8
LEARNING_RATE = 0.0001
MAX_IMAGES_PER_CLASS = 80

def get_subset(dataset, max_images=45):
    indices = []
    targets = np.array(dataset.targets)
    for class_idx in range(len(dataset.classes)):
        class_name = dataset.classes[class_idx]
        class_indices = np.where(targets == class_idx)[0]
        if len(class_indices) == 0:
            continue
            
        # For training, oversample minority classes to balance
        if max_images > 10:
            if class_name == "Normal Skin":
                target_count = 160
            elif class_name == "Non-Skin":
                target_count = 120
            else:
                target_count = max_images
        else:
            target_count = max_images
            
        if len(class_indices) < target_count:
            np.random.seed(42)
            selected_indices = np.random.choice(class_indices, size=target_count, replace=True)
        else:
            selected_indices = class_indices[:target_count]
        indices.extend(selected_indices)
    return Subset(dataset, indices)


def train():
    print("--- STARTING SUBSET-BASED ULTRA FAST TRAINING ---")
    
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
    full_train_dataset = datasets.ImageFolder(TRAIN_DIR, data_transforms['train'])
    full_val_dataset = datasets.ImageFolder(VAL_DIR, data_transforms['val'])
    
    class_names = full_train_dataset.classes
    with open(LABELS_SAVE_PATH, 'w') as f:
        json.dump(class_names, f)
    
    print(f"Classes: {len(class_names)}")
    
    train_subset = get_subset(full_train_dataset, max_images=MAX_IMAGES_PER_CLASS)
    val_subset = get_subset(full_val_dataset, max_images=5)
    
    print(f"Subset sizes -> Train: {len(train_subset)}, Val: {len(val_subset)}")
    
    dataloaders = {
        'train': DataLoader(train_subset, batch_size=BATCH_SIZE, shuffle=True, num_workers=0),
        'val': DataLoader(val_subset, batch_size=BATCH_SIZE, shuffle=False, num_workers=0)
    }

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")
    
    # Use full model fine-tuning
    model = ai_model.get_model(len(class_names), fine_tune=True).to(device)
    
    criterion = nn.CrossEntropyLoss()
    # Optimize all model parameters
    optimizer = optim.Adam(model.parameters(), lr=LEARNING_RATE)
    
    for epoch in range(EPOCHS):
        model.train()
        running_loss = 0.0
        running_corrects = 0
        
        for inputs, labels in dataloaders['train']:
            inputs, labels = inputs.to(device), labels.to(device)
            optimizer.zero_grad()
            outputs = model(inputs)
            _, preds = torch.max(outputs, 1)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()
            
            running_loss += loss.item() * inputs.size(0)
            running_corrects += torch.sum(preds == labels.data)
        
        epoch_loss = running_loss / len(train_subset)
        epoch_acc = running_corrects.double() / len(train_subset)
        print(f"Epoch {epoch+1}/{EPOCHS} | Train Loss: {epoch_loss:.4f} Acc: {epoch_acc:.4f}")
        
        # Validation
        model.eval()
        val_corrects = 0
        with torch.no_grad():
            for inputs, labels in dataloaders['val']:
                inputs, labels = inputs.to(device), labels.to(device)
                outputs = model(inputs)
                _, preds = torch.max(outputs, 1)
                val_corrects += torch.sum(preds == labels.data)
        
        val_acc = val_corrects.double() / len(val_subset)
        print(f"Validation Acc: {val_acc:.4f}")
        
        # Save checkpoint
        torch.save(model.state_dict(), MODEL_SAVE_PATH)
        print("Model saved successfully.")

    print("\nTraining Complete!")

if __name__ == "__main__":
    train()
