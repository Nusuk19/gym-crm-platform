package com.gym.crm.core.mapper;

import com.gia.openapi.model.LoginChangeRequest;
import com.gia.openapi.model.LoginRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.dto.request.UserCredentials;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    UserCredentials toCredentials(LoginRequest request);

    ChangePasswordRequest toChangePassword(LoginChangeRequest request);
}