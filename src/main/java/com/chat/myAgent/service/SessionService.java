package com.chat.myAgent.service;

import com.chat.myAgent.model.vo.SessionVO;

public interface SessionService {

    void saveSession(String token, SessionVO sessionVO);

    SessionVO getSession(String token);

    void deleteSession(String token);
}
