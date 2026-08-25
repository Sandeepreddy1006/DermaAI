from pydantic import BaseModel, EmailStr
from typing import List, Optional
from datetime import datetime

class UserBase(BaseModel):
    email: EmailStr
    full_name: str
    avatar_url: Optional[str] = None

class UserCreate(UserBase):
    password: str

class UserUpdate(BaseModel):
    full_name: Optional[str] = None
    email: Optional[EmailStr] = None

class PasswordResetRequest(BaseModel):
    email: EmailStr

class User(UserBase):
    id: int
    created_at: datetime

    class Config:
        from_attributes = True

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    email: Optional[str] = None

class AnalysisBase(BaseModel):
    result_title: str
    result_description: str
    confidence_score: int
    precautions: Optional[str] = None
    first_aid: Optional[str] = None

class AnalysisCreate(AnalysisBase):
    image_url: str

class Analysis(AnalysisBase):
    id: int
    image_url: str
    created_at: datetime
    user_id: int

    class Config:
        from_attributes = True

class Doctor(BaseModel):
    id: int
    name: str
    specialty: str
    rating: float
    distance: str
    address: str
    image_url: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None

    class Config:
        from_attributes = True

class VerificationRequest(BaseModel):
    email: EmailStr
    code: str

class NewPasswordRequest(BaseModel):
    email: EmailStr
    code: str
    new_password: str
