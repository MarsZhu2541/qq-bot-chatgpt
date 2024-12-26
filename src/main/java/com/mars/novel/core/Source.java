package com.mars.novel.core;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.mars.novel.model.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class Source {

    public Rule rule;
    public Source(int id) {
        this.rule = Rule.initRules().get(id);
    }

    public void setSourceId(int id) {
        this.rule = Rule.initRules().get(id);
    }
}
