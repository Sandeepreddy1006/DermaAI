import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
import os
import json
from PIL import Image
import numpy as np
import ai_model

# Configuration
DATA_DIR = r"c:\DermaCareAI_New\backend\dataset"
TRAIN_DIR = os.path.join(DATA_DIR, "train")
VAL_DIR = os.path.join(DATA_DIR, "test")
MODEL_SAVE_PATH = r"c:\DermaCareAI_New\backend\skin_model.pth"
LABELS_SAVE_PATH = r"c:\DermaCareAI_New\backend\labels.json"
BATCH_SIZE = 32
EPOCHS = 10
LEARNING_RATE = 0.0001

class Skin5Dataset(Dataset):
    def __init__(self, root_dir, transform=None, is_train=True):
        self.root_dir = root_dir
        self.transform = transform
        self.is_train = is_train
        
        # Mapping from directory names to class index
        self.class_mapping = {
            "Acne and Rosacea Photos": 0,
            "Eczema Photos": 1,
            "Psoriasis pictures Lichen Planus and related diseases": 2,
            "Melanoma Skin Cancer Nevi and Moles": 3,
            "Normal Skin": 4
        }
        self.classes = ["Acne", "Eczema", "Psoriasis", "Melanoma", "Normal Skin"]
        
        self.image_paths = []
        self.labels = []
        
        # Load and balance images
        for dir_name, label in self.class_mapping.items():
            dir_path = os.path.join(root_dir, dir_name)
            if not os.path.exists(dir_path):
                print(f"Warning: Directory not found: {dir_path}")
                continue
                
            files = [f for f in os.listdir(dir_path) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]
            
            # Select subset to keep it fast & balanced
            if is_train:
                if label == 4: # Normal Skin
                    # Duplicate to balance (we have 30 images, duplicate 7x to 210)
                    selected_files = files * 7
                else:
                    selected_files = files[:200]
            else:
                if label == 4:
                    # Duplicate to balance (we have 10 images, duplicate 4x to 40)
                    selected_files = files * 4
                else:
                    selected_files = files[:40]
            
            for f in selected_files:
                self.image_paths.append(os.path.join(dir_path, f))
                self.labels.append(label)
                
        print(f"Loaded {len(self.image_paths)} images for {'train' if is_train else 'val'}")

    def __len__(self):
        return len(self.image_paths)

    def __getitem__(self, idx):
        path = self.image_paths[idx]
        try:
            img = Image.open(path).convert('RGB')
        except Exception as e:
            # Fallback for broken images
            img = Image.new('RGB', (224, 224), color='white')
            
        label = self.labels[idx]
        if self.transform:
            img = self.transform(img)
        return img, torch.tensor(label, dtype=torch.long)

def train():
    print("--- STARTING 5-CLASS MODEL TRAINING ---")
    
    # Advanced Data Augmentation for training
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
    
    train_dataset = Skin5Dataset(TRAIN_DIR, data_transforms['train'], is_train=True)
    val_dataset = Skin5Dataset(VAL_DIR, data_transforms['val'], is_train=False)
    
    train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True, num_workers=0)
    val_loader = DataLoader(val_dataset, batch_size=BATCH_SIZE, shuffle=False, num_workers=0)
    
    # Save the 5 classes to labels.json
    with open(LABELS_SAVE_PATH, 'w') as f:
        json.dump(train_dataset.classes, f)
    print(f"Saved classes to {LABELS_SAVE_PATH}")
    
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")
    
    # Retrieve base model architecture (MobileNetV2) and replace classification layer with fine-tuning enabled
    model = ai_model.get_model(len(train_dataset.classes), fine_tune=True).to(device)
    
    criterion = nn.CrossEntropyLoss()
    # Optimize ALL model parameters with a low learning rate for best fine-tuning accuracy
    optimizer = optim.Adam(model.parameters(), lr=0.0001)
    
    for epoch in range(EPOCHS):
        model.train()
        running_loss = 0.0
        running_corrects = 0
        
        for inputs, labels in train_loader:
            inputs, labels = inputs.to(device), labels.to(device)
            optimizer.zero_grad()
            outputs = model(inputs)
            _, preds = torch.max(outputs, 1)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()
            
            running_loss += loss.item() * inputs.size(0)
            running_corrects += torch.sum(preds == labels.data)
            
        epoch_loss = running_loss / len(train_dataset)
        epoch_acc = running_corrects.double() / len(train_dataset)
        print(f"Epoch {epoch+1}/{EPOCHS} | Train Loss: {epoch_loss:.4f} Acc: {epoch_acc:.4f}")
        
        # Validation
        model.eval()
        val_corrects = 0
        with torch.no_grad():
            for inputs, labels in val_loader:
                inputs, labels = inputs.to(device), labels.to(device)
                outputs = model(inputs)
                _, preds = torch.max(outputs, 1)
                val_corrects += torch.sum(preds == labels.data)
                
        val_acc = val_corrects.double() / len(val_dataset)
        print(f"Validation Acc: {val_acc:.4f}")
        
        # Save model checkpoint
        torch.save(model.state_dict(), MODEL_SAVE_PATH)
        print(f"Model saved to {MODEL_SAVE_PATH}")

    print("\nTraining Complete!")

if __name__ == "__main__":
    train()
