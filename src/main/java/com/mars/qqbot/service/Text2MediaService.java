package com.mars.qqbot.service;

import com.mars.qqbot.model.QqMedia;

public interface Text2MediaService {
    QqMedia getMedia(String msg);
}
