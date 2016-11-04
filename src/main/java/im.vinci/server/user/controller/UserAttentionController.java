package im.vinci.server.user.controller;

import im.vinci.server.security.ApiSecurityLabel;
import im.vinci.server.user.service.UserAttentionService;
import im.vinci.server.utils.UserContext;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户关注相关的接口
 * Created by mayuchen on 16/8/2.
 */
@RestController
@RequestMapping(value = "/vinci/user", produces = "application/json;charset=UTF-8")
public class UserAttentionController {

    @Autowired
    private UserAttentionService userAttentionService;      //UserContext.getUserInfo().getId()

    //添加某用户关注
    @RequestMapping(value = "/attention/add", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject AttentionAdd(@RequestParam("attention_uid") long attentionUid){
        return new ResultObject<>(userAttentionService.insertUserAttention(UserContext.getUserInfo().getId(), attentionUid));
    }


    //删除某用户关注
    @RequestMapping(value = "/attention/delete", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject AttentionDelete(@RequestParam("attention_uid") long attentionUid){
        return new ResultObject<>(userAttentionService.deleteUserAttention(UserContext.getUserInfo().getId(), attentionUid));
    }

    //获取用户关注对应用户信息列表
    @RequestMapping(value = "/attention/userlist")
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<List> AttentionUserList(@RequestParam("attention_uid") long attentionUid,
                                                @RequestParam("last_attention_id") long lastAttentionId,
                                                @RequestParam("page_size") long pageSize){

        return new ResultObject<List>(userAttentionService.getUserAttentionerList(attentionUid, lastAttentionId, pageSize));
    }

    //获取用户粉丝列对应用户信息表
    @RequestMapping(value = "/attention/followlist")
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject AttentionFollowList(@RequestParam("attention_uid") long attentionUid,
                                            @RequestParam("last_attention_id") long lastAttentionId,
                                            @RequestParam("page_size") long pageSize){
        return new ResultObject<List>(userAttentionService.getUserFollowerList(attentionUid, lastAttentionId, pageSize));
    }
}
