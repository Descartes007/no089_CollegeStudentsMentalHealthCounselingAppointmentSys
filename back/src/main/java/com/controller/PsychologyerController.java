
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
 * 心理老师
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/psychologyer")
public class PsychologyerController {
    private static final Logger logger = LoggerFactory.getLogger(PsychologyerController.class);

    @Autowired
    private PsychologyerService psychologyerService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service

    @Autowired
    private YonghuService yonghuService;


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
        PageUtils page = psychologyerService.queryPage(params);

        //字典表数据转换
        List<PsychologyerView> list =(List<PsychologyerView>)page.getList();
        for(PsychologyerView c:list){
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
        PsychologyerEntity psychologyer = psychologyerService.selectById(id);
        if(psychologyer !=null){
            //entity转view
            PsychologyerView view = new PsychologyerView();
            BeanUtils.copyProperties( psychologyer , view );//把实体数据重构到view中

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
    public R save(@RequestBody PsychologyerEntity psychologyer, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,psychologyer:{}",this.getClass().getName(),psychologyer.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<PsychologyerEntity> queryWrapper = new EntityWrapper<PsychologyerEntity>()
            .eq("username", psychologyer.getUsername())
            .or()
            .eq("psychologyer_phone", psychologyer.getPsychologyerPhone())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        PsychologyerEntity psychologyerEntity = psychologyerService.selectOne(queryWrapper);
        if(psychologyerEntity==null){
            psychologyer.setCreateTime(new Date());
            psychologyer.setPassword("123456");
            psychologyerService.insert(psychologyer);
            return R.ok();
        }else {
            return R.error(511,"账户或者心理老师手机号已经被使用");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody PsychologyerEntity psychologyer, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,psychologyer:{}",this.getClass().getName(),psychologyer.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        //根据字段查询是否有相同数据
        Wrapper<PsychologyerEntity> queryWrapper = new EntityWrapper<PsychologyerEntity>()
            .notIn("id",psychologyer.getId())
            .andNew()
            .eq("username", psychologyer.getUsername())
            .or()
            .eq("psychologyer_phone", psychologyer.getPsychologyerPhone())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        PsychologyerEntity psychologyerEntity = psychologyerService.selectOne(queryWrapper);
        if("".equals(psychologyer.getPsychologyerPhoto()) || "null".equals(psychologyer.getPsychologyerPhoto())){
                psychologyer.setPsychologyerPhoto(null);
        }
        if(psychologyerEntity==null){
            psychologyerService.updateById(psychologyer);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"账户或者心理老师手机号已经被使用");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        psychologyerService.deleteBatchIds(Arrays.asList(ids));
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
            List<PsychologyerEntity> psychologyerList = new ArrayList<>();//上传的东西
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
                            PsychologyerEntity psychologyerEntity = new PsychologyerEntity();
//                            psychologyerEntity.setUsername(data.get(0));                    //账户 要改的
//                            //psychologyerEntity.setPassword("123456");//密码
//                            psychologyerEntity.setPsychologyerName(data.get(0));                    //心理老师姓名 要改的
//                            psychologyerEntity.setPsychologyerPhone(data.get(0));                    //心理老师手机号 要改的
//                            psychologyerEntity.setPsychologyerPhoto("");//详情和图片
//                            psychologyerEntity.setSexTypes(Integer.valueOf(data.get(0)));   //性别 要改的
//                            psychologyerEntity.setPsychologyerEmail(data.get(0));                    //电子邮箱 要改的
//                            psychologyerEntity.setPsychologyerShanchang(data.get(0));                    //擅长 要改的
//                            psychologyerEntity.setPsychologyerLvli(data.get(0));                    //履历 要改的
//                            psychologyerEntity.setPsychologyerContent("");//详情和图片
//                            psychologyerEntity.setCreateTime(date);//时间
                            psychologyerList.add(psychologyerEntity);


                            //把要查询是否重复的字段放入map中
                                //账户
                                if(seachFields.containsKey("username")){
                                    List<String> username = seachFields.get("username");
                                    username.add(data.get(0));//要改的
                                }else{
                                    List<String> username = new ArrayList<>();
                                    username.add(data.get(0));//要改的
                                    seachFields.put("username",username);
                                }
                                //心理老师手机号
                                if(seachFields.containsKey("psychologyerPhone")){
                                    List<String> psychologyerPhone = seachFields.get("psychologyerPhone");
                                    psychologyerPhone.add(data.get(0));//要改的
                                }else{
                                    List<String> psychologyerPhone = new ArrayList<>();
                                    psychologyerPhone.add(data.get(0));//要改的
                                    seachFields.put("psychologyerPhone",psychologyerPhone);
                                }
                        }

                        //查询是否重复
                         //账户
                        List<PsychologyerEntity> psychologyerEntities_username = psychologyerService.selectList(new EntityWrapper<PsychologyerEntity>().in("username", seachFields.get("username")));
                        if(psychologyerEntities_username.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(PsychologyerEntity s:psychologyerEntities_username){
                                repeatFields.add(s.getUsername());
                            }
                            return R.error(511,"数据库的该表中的 [账户] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                         //心理老师手机号
                        List<PsychologyerEntity> psychologyerEntities_psychologyerPhone = psychologyerService.selectList(new EntityWrapper<PsychologyerEntity>().in("psychologyer_phone", seachFields.get("psychologyerPhone")));
                        if(psychologyerEntities_psychologyerPhone.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(PsychologyerEntity s:psychologyerEntities_psychologyerPhone){
                                repeatFields.add(s.getPsychologyerPhone());
                            }
                            return R.error(511,"数据库的该表中的 [心理老师手机号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        psychologyerService.insertBatch(psychologyerList);
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
    * 登录
    */
    @IgnoreAuth
    @RequestMapping(value = "/login")
    public R login(String username, String password, String captcha, HttpServletRequest request) {
        PsychologyerEntity psychologyer = psychologyerService.selectOne(new EntityWrapper<PsychologyerEntity>().eq("username", username));
        if(psychologyer==null || !psychologyer.getPassword().equals(password))
            return R.error("账号或密码不正确");
        //  // 获取监听器中的字典表
        // ServletContext servletContext = ContextLoader.getCurrentWebApplicationContext().getServletContext();
        // Map<String, Map<Integer, String>> dictionaryMap= (Map<String, Map<Integer, String>>) servletContext.getAttribute("dictionaryMap");
        // Map<Integer, String> role_types = dictionaryMap.get("role_types");
        // role_types.get(.getRoleTypes());
        String token = tokenService.generateToken(psychologyer.getId(),username, "psychologyer", "心理老师");
        R r = R.ok();
        r.put("token", token);
        r.put("role","心理老师");
        r.put("username",psychologyer.getPsychologyerName());
        r.put("tableName","psychologyer");
        r.put("userId",psychologyer.getId());
        return r;
    }

    /**
    * 注册
    */
    @IgnoreAuth
    @PostMapping(value = "/register")
    public R register(@RequestBody PsychologyerEntity psychologyer){
//    	ValidatorUtils.validateEntity(user);
        Wrapper<PsychologyerEntity> queryWrapper = new EntityWrapper<PsychologyerEntity>()
            .eq("username", psychologyer.getUsername())
            .or()
            .eq("psychologyer_phone", psychologyer.getPsychologyerPhone())
            ;
        PsychologyerEntity psychologyerEntity = psychologyerService.selectOne(queryWrapper);
        if(psychologyerEntity != null)
            return R.error("账户或者心理老师手机号已经被使用");
        psychologyer.setCreateTime(new Date());
        psychologyerService.insert(psychologyer);
        return R.ok();
    }

    /**
     * 重置密码
     */
    @GetMapping(value = "/resetPassword")
    public R resetPassword(Integer  id){
        PsychologyerEntity psychologyer = new PsychologyerEntity();
        psychologyer.setPassword("123456");
        psychologyer.setId(id);
        psychologyerService.updateById(psychologyer);
        return R.ok();
    }


    /**
     * 忘记密码
     */
    @IgnoreAuth
    @RequestMapping(value = "/resetPass")
    public R resetPass(String username, HttpServletRequest request) {
        PsychologyerEntity psychologyer = psychologyerService.selectOne(new EntityWrapper<PsychologyerEntity>().eq("username", username));
        if(psychologyer!=null){
            psychologyer.setPassword("123456");
            boolean b = psychologyerService.updateById(psychologyer);
            if(!b){
               return R.error();
            }
        }else{
           return R.error("账号不存在");
        }
        return R.ok();
    }


    /**
    * 获取学生的session学生信息
    */
    @RequestMapping("/session")
    public R getCurrPsychologyer(HttpServletRequest request){
        Integer id = (Integer)request.getSession().getAttribute("userId");
        PsychologyerEntity psychologyer = psychologyerService.selectById(id);
        if(psychologyer !=null){
            //entity转view
            PsychologyerView view = new PsychologyerView();
            BeanUtils.copyProperties( psychologyer , view );//把实体数据重构到view中

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }
    }


    /**
    * 退出
    */
    @GetMapping(value = "logout")
    public R logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return R.ok("退出成功");
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
        PageUtils page = psychologyerService.queryPage(params);

        //字典表数据转换
        List<PsychologyerView> list =(List<PsychologyerView>)page.getList();
        for(PsychologyerView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        PsychologyerEntity psychologyer = psychologyerService.selectById(id);
            if(psychologyer !=null){


                //entity转view
                PsychologyerView view = new PsychologyerView();
                BeanUtils.copyProperties( psychologyer , view );//把实体数据重构到view中

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
    public R add(@RequestBody PsychologyerEntity psychologyer, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,psychologyer:{}",this.getClass().getName(),psychologyer.toString());
        Wrapper<PsychologyerEntity> queryWrapper = new EntityWrapper<PsychologyerEntity>()
            .eq("username", psychologyer.getUsername())
            .or()
            .eq("psychologyer_phone", psychologyer.getPsychologyerPhone())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        PsychologyerEntity psychologyerEntity = psychologyerService.selectOne(queryWrapper);
        if(psychologyerEntity==null){
            psychologyer.setCreateTime(new Date());
        psychologyer.setPassword("123456");
        psychologyerService.insert(psychologyer);
            return R.ok();
        }else {
            return R.error(511,"账户或者心理老师手机号已经被使用");
        }
    }


}
