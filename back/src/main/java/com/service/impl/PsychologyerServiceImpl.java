package com.service.impl;

import com.utils.StringUtil;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.util.*;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.dao.PsychologyerDao;
import com.entity.PsychologyerEntity;
import com.service.PsychologyerService;
import com.entity.view.PsychologyerView;

/**
 * 心理老师 服务实现类
 */
@Service("psychologyerService")
@Transactional
public class PsychologyerServiceImpl extends ServiceImpl<PsychologyerDao, PsychologyerEntity> implements PsychologyerService {

    @Override
    public PageUtils queryPage(Map<String,Object> params) {
        if(params != null && (params.get("limit") == null || params.get("page") == null)){
            params.put("page","1");
            params.put("limit","10");
        }
        Page<PsychologyerView> page =new Query<PsychologyerView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page,params));
        return new PageUtils(page);
    }


}
