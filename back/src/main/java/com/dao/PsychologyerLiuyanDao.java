package com.dao;

import com.entity.PsychologyerLiuyanEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.view.PsychologyerLiuyanView;

/**
 * 心理老师留言 Dao 接口
 *
 * @author 
 */
public interface PsychologyerLiuyanDao extends BaseMapper<PsychologyerLiuyanEntity> {

   List<PsychologyerLiuyanView> selectListView(Pagination page,@Param("params")Map<String,Object> params);

}
