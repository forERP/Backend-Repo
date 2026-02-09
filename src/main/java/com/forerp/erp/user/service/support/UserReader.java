package com.forerp.erp.user.service.support;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserReader {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public User getUser(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
    public User getUserByLoginId(String loginId){
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디가 일치하지 않습니다."));
    }
    public User getUserForPos(String storeCode, String employeeCode){
        return userRepository.findByStore_StoreAndEmployeeCode(storeCode, employeeCode)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
    }
    public Store getStore(Long storeId){
        if(storeId == null) return null;
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
    }
    public void validateNewUser(String loginId){
        if(userRepository.existsByLoginId(loginId)){
            throw new IllegalArgumentException("이미 사용 중인 로그인 ID 입니다.");
        }
    }
    public long getNextEmployeeSequence(Store store){
        if(store == null) return 0;
        return userRepository.countByStore(store) + 1;
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
}
