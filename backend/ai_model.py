import torch
import torch.nn as nn
from torchvision import models, transforms
from PIL import Image
import os
import json

def get_model(num_classes, fine_tune=True):
    # Use MobileNetV2 for best balance of speed and accuracy on mobile
    model = models.mobilenet_v2(pretrained=True)
    
    if fine_tune:
        # Unfreeze all layers for specialized learning
        for param in model.parameters():
            param.requires_grad = True
    else:
        # Freeze base features (initial training)
        for param in model.parameters():
            param.requires_grad = False
    
    # Replace the classifier with a new one for our 23 classes
    num_ftrs = model.classifier[1].in_features
    model.classifier[1] = nn.Sequential(
        nn.Dropout(0.3), # Increased dropout to prevent overfitting during fine-tuning
        nn.Linear(num_ftrs, num_classes)
    )
    return model

def predict_skin_condition(image_path, model_path, labels_path):
    if not os.path.exists(model_path) or not os.path.exists(labels_path):
        return None, 0
    
    with open(labels_path, 'r') as f:
        labels = json.load(f)
    
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = get_model(len(labels), fine_tune=True).to(device)
    model.load_state_dict(torch.load(model_path, map_location=device))
    model.eval()
    
    # Use standard torchvision transforms
    preprocess = transforms.Compose([
        transforms.Resize(256),
        transforms.CenterCrop(224),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
    ])
    
    image = Image.open(image_path).convert('RGB')
    image_tensor = preprocess(image).unsqueeze(0).to(device)
    
    with torch.no_grad():
        outputs = model(image_tensor)
        probabilities = torch.nn.functional.softmax(outputs, dim=1)
        confidence, predicted = torch.max(probabilities, 1)
        
    return labels[predicted.item()], confidence.item() * 100
