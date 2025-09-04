from sqlalchemy import Column, Integer, String, DateTime, Text, Identity
from sqlalchemy.sql import func
from .database import Base

class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, Identity(start=1), primary_key=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    email = Column(String(100), unique=True, index=True, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

class HealthCheck(Base):
    __tablename__ = "health_check"
    
    id = Column(Integer, Identity(start=1), primary_key=True)
    status = Column(String(20), nullable=False)
    checked_at = Column(DateTime(timezone=True), server_default=func.now())