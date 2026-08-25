import torch
import json
import os
from ai_model import get_model

DATA_DIR = r"c:\DermaCareAI_New\backend\dataset"
TRAIN_DIR = os.path.join(DATA_DIR, "train")

print("Quickly generating model and labels...")

# get classes directly from folders
class_names = [d for d in sorted(os.listdir(TRAIN_DIR)) if os.path.isdir(os.path.join(TRAIN_DIR, d))]

with open(r"c:\DermaCareAI_New\backend\labels.json", 'w') as f:
    json.dump(class_names, f)

print(f"Saved {len(class_names)} classes to labels.json.")

# Generate model
model = get_model(len(class_names), fine_tune=True)
torch.save(model.state_dict(), r"c:\DermaCareAI_New\backend\skin_model.pth")
print("Saved base model to skin_model.pth.")
print("Fast setup complete! The backend is now ready for testing.")
