package com.jira.clone.service;

import com.jira.clone.dto.SignupRequest;
import com.jira.clone.dto.LoginRequest;
import com.jira.clone.model.User;

public interface AuthService {
    User registerUser(SignupRequest signupRequest);
    User loginUser(LoginRequest loginRequest);
}