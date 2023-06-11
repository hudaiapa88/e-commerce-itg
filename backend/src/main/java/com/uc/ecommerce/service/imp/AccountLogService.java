package com.uc.ecommerce.service.imp;

import com.uc.ecommerce.model.entity.account.Account;
import com.uc.ecommerce.model.entity.account.User;
import com.uc.ecommerce.model.entity.log.AccountLog;
import com.uc.ecommerce.model.enums.AccountLogType;
import jakarta.transaction.Transactional;

import java.util.List;

public interface AccountLogService {

    void save(Account account, String title, AccountLogType accountLogType);

    List<AccountLog> findByAccount_IdAndAccountLogType(Long userId, AccountLogType accountLogType);

    void deleteByAccount_IdAndAccountLogType(Long id, AccountLogType wrongEntry);
}
