
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 心理咨询预约申请
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/psychologyerOrder")
public class PsychologyerOrderController {
    private static final Logger logger = LoggerFactory.getLogger(PsychologyerOrderController.class);

    @Autowired
    private PsychologyerOrderService psychologyerOrderService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private YonghuService yonghuService;
    @Autowired
    private PsychologyerService psychologyerService;



    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("学生".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("心理老师".equals(role))
            params.put("psychologyerId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = psychologyerOrderService.queryPage(params);

        //字典表数据转换
        List<PsychologyerOrderView> list =(List<PsychologyerOrderView>)page.getList();
        for(PsychologyerOrderView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        PsychologyerOrderEntity psychologyerOrder = psychologyerOrderService.selectById(id);
        if(psychologyerOrder !=null){
            //entity转view
            PsychologyerOrderView view = new PsychologyerOrderView();
            BeanUtils.copyProperties( psychologyerOrder , view );//把实体数据重构到view中

                //级联表
                YonghuEntity yonghu = yonghuService.selectById(psychologyerOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //级联表
                PsychologyerEntity psychologyer = psychologyerService.selectById(psychologyerOrder.getPsychologyerId());
                if(psychologyer != null){
                    BeanUtils.copyProperties( psychologyer , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setPsychologyerId(psychologyer.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody PsychologyerOrderEntity psychologyerOrder, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,psychologyerOrder:{}",this.getClass().getName(),psychologyerOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("心理老师".equals(role))
            psychologyerOrder.setPsychologyerId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        else if("学生".equals(role))
            psychologyerOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        psychologyerOrder.setInsertTime(new Date());
        psychologyerOrder.setCreateTime(new Date());
        psychologyerOrderService.insert(psychologyerOrder);
        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody PsychologyerOrderEntity psychologyerOrder, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,psychologyerOrder:{}",this.getClass().getName(),psychologyerOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("心理老师".equals(role))
//            psychologyerOrder.setPsychologyerId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
//        else if("学生".equals(role))
//            psychologyerOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<PsychologyerOrderEntity> queryWrapper = new EntityWrapper<PsychologyerOrderEntity>()
            .eq("id",0)
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        PsychologyerOrderEntity psychologyerOrderEntity = psychologyerOrderService.selectOne(queryWrapper);
        if(psychologyerOrderEntity==null){
            psychologyerOrderService.updateById(psychologyerOrder);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        psychologyerOrderService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<PsychologyerOrderEntity> psychologyerOrderList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            PsychologyerOrderEntity psychologyerOrderEntity = new PsychologyerOrderEntity();
//                            psychologyerOrderEntity.setPsychologyerOrderUuidNumber(data.get(0));                    //预约流水号 要改的
//                            psychologyerOrderEntity.setPsychologyerId(Integer.valueOf(data.get(0)));   //心理老师 要改的
//                            psychologyerOrderEntity.setYonghuId(Integer.valueOf(data.get(0)));   //学生 要改的
//                            psychologyerOrderEntity.setYuyueTime(sdf.parse(data.get(0)));          //预约日期 要改的
//                            psychologyerOrderEntity.setShijianduanTypes(Integer.valueOf(data.get(0)));   //预约时间段 要改的
//                            psychologyerOrderEntity.setPsychologyerOrderYesnoTypes(Integer.valueOf(data.get(0)));   //预约状态 要改的
//                            psychologyerOrderEntity.setPsychologyerOrderYesnoText(data.get(0));                    //审核意见 要改的
//                            psychologyerOrderEntity.setInsertTime(date);//时间
//                            psychologyerOrderEntity.setCreateTime(date);//时间
                            psychologyerOrderList.add(psychologyerOrderEntity);


                            //把要查询是否重复的字段放入map中
                                //预约流水号
                                if(seachFields.containsKey("psychologyerOrderUuidNumber")){
                                    List<String> psychologyerOrderUuidNumber = seachFields.get("psychologyerOrderUuidNumber");
                                    psychologyerOrderUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> psychologyerOrderUuidNumber = new ArrayList<>();
                                    psychologyerOrderUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("psychologyerOrderUuidNumber",psychologyerOrderUuidNumber);
                                }
                        }

                        //查询是否重复
                         //预约流水号
                        List<PsychologyerOrderEntity> psychologyerOrderEntities_psychologyerOrderUuidNumber = psychologyerOrderService.selectList(new EntityWrapper<PsychologyerOrderEntity>().in("psychologyer_order_uuid_number", seachFields.get("psychologyerOrderUuidNumber")));
                        if(psychologyerOrderEntities_psychologyerOrderUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(PsychologyerOrderEntity s:psychologyerOrderEntities_psychologyerOrderUuidNumber){
                                repeatFields.add(s.getPsychologyerOrderUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [预约流水号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        psychologyerOrderService.insertBatch(psychologyerOrderList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = psychologyerOrderService.queryPage(params);

        //字典表数据转换
        List<PsychologyerOrderView> list =(List<PsychologyerOrderView>)page.getList();
        for(PsychologyerOrderView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        PsychologyerOrderEntity psychologyerOrder = psychologyerOrderService.selectById(id);
            if(psychologyerOrder !=null){


                //entity转view
                PsychologyerOrderView view = new PsychologyerOrderView();
                BeanUtils.copyProperties( psychologyerOrder , view );//把实体数据重构到view中

                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(psychologyerOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //级联表
                    PsychologyerEntity psychologyer = psychologyerService.selectById(psychologyerOrder.getPsychologyerId());
                if(psychologyer != null){
                    BeanUtils.copyProperties( psychologyer , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setPsychologyerId(psychologyer.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody PsychologyerOrderEntity psychologyerOrder, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,psychologyerOrder:{}",this.getClass().getName(),psychologyerOrder.toString());
            PsychologyerEntity psychologyerEntity = psychologyerService.selectById(psychologyerOrder.getPsychologyerId());
            if(psychologyerEntity == null){
                return R.error(511,"查不到该心理老师");
            }
            // Double psychologyerNewMoney = psychologyerEntity.getPsychologyerNewMoney();

            if(false){
            }

            //计算所获得积分
            Double buyJifen =0.0;
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            psychologyerOrder.setYonghuId(userId); //设置订单支付人id
            psychologyerOrder.setPsychologyerOrderUuidNumber(String.valueOf(new Date().getTime()));
            psychologyerOrder.setInsertTime(new Date());
            psychologyerOrder.setCreateTime(new Date());
                psychologyerOrderService.insert(psychologyerOrder);//新增订单
            return R.ok();
    }



}
