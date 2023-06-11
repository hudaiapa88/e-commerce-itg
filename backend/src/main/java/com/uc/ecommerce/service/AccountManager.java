package com.uc.ecommerce.service;


import com.uc.ecommerce.controller.account.LoginRequest;
import com.uc.ecommerce.controller.account.LoginResponse;
import com.uc.ecommerce.core.exception.EntityNotFoundException;
import com.uc.ecommerce.core.exception.IncorrectEntryException;
import com.uc.ecommerce.core.exception.validator.UserDisabledException;
import com.uc.ecommerce.core.security.CustomAccountDetails;
import com.uc.ecommerce.core.security.JwtTokenUtil;
import com.uc.ecommerce.core.security.SecurityContextUtil;
import com.uc.ecommerce.core.validator.VerificationCodeValidator;
import com.uc.ecommerce.core.validator.WrongEntryValidator;
import com.uc.ecommerce.model.entity.account.Account;
import com.uc.ecommerce.model.entity.account.Admin;
import com.uc.ecommerce.model.entity.account.User;
import com.uc.ecommerce.model.enums.AccountLogType;
import com.uc.ecommerce.repository.AccountRepository;
import com.uc.ecommerce.service.imp.AccountLogService;
import com.uc.ecommerce.service.imp.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Service
@RequiredArgsConstructor
public class AccountManager implements AccountService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final AccountRepository accountRepository;
    private final VerificationCodeValidator verificationCodeValidator;
    private final SecurityContextUtil securityContextUtil;
    private final AccountLogService accountLogService;
    private final WrongEntryValidator wrongEntryValidator;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            CustomAccountDetails customUserDetails = (CustomAccountDetails) authentication.getPrincipal();
            Account currentUser = customUserDetails.getAccount();
            final String token = jwtTokenUtil.generate(customUserDetails, loginRequest.isRememberMe());
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authentication);
            HttpServletRequest request = securityContextUtil.getCurrentHttpRequest().get();
            HttpSession session = request.getSession(true);
            session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, sc);
            return new LoginResponse(currentUser.getUsername(), token, currentUser.getRole());

        } catch (BadCredentialsException badCredentialsException) {
            Account account = findByUserName(loginRequest.getUsername());
            if (account instanceof User) {
                accountLogService.save(account, "Wrong entry", AccountLogType.WRONG_ENTRY);
                if (wrongEntryValidator.validate(account)) {
                    disableAccount(account);
                }
            }
            throw new IncorrectEntryException("Kullanıcı adını veya şifreyi yanlış girdiniz.");

        } catch (DisabledException disabledException) {
            throw new UserDisabledException("Hesabınızın Yönetici tarafından Onaylanması bekleniyor.");
        }
    }

    @Override
    public void disableAccount(Account account) {
        account.setIsActive(Boolean.FALSE);
        accountRepository.save(account);
    }

    @Override
    public Account findByUserName(String username) {
        return accountRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Kullanıcı adı bulunamadı, lütfen kontrol ediniz ."));
    }

    @Override
    public Boolean existByUsername(String s) {
        return accountRepository.existsByUsername(s);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth);

    }

    @Override
    public void active(Long id, String code) {
        Account account = findById(id);
        verificationCodeValidator.validate(account, code);
        account.setIsActive(true);
        accountRepository.save(account);
    }

    @Override
    public boolean existByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }




    @Override
    public Account findById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı."));
    }


}
