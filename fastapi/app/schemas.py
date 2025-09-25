from pydantic import BaseModel
from datetime import datetime
from typing import Optional

from enum import Enum


class UserBase(BaseModel):
    username: str
    email: str

class UserCreate(UserBase):
    pass

class User(UserBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True

class HealthCheckResponse(BaseModel):
    status: str
    database_status: str
    timestamp: datetime

# publish Pydantic model, Enum
class PublishStatus(str, Enum):
    SUCCESS = "SUCCESS"
    FAILED = "FAILED"
    PENDING = "PENDING"

class PublishRequest(BaseModel):
    aiContentId: int
    title: Optional[str] = None
    metaDescription: Optional[str] = None
    markdown: Optional[str] = None
    hashtag: Optional[str] = None

class PublishResponse(BaseModel):
    publishStatus: PublishStatus
    blogPostId: Optional[str] = None
    blogUrl: Optional[str] = None
    publishResponse: Optional[str] = None
    errorMessage: Optional[str] = None

class SsadaguCrawlRequest(BaseModel):
    keyword: str
    execution_id: int

class SsadaguCrawlResponse(BaseModel):
    success: bool
    execution_id: int
    product_name: Optional[str] = None
    product_url: Optional[str] = None
    price: Optional[int] = None
    crawling_method: Optional[str] = None
    error_message: Optional[str] = None