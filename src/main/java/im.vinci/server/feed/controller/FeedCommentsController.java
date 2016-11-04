package im.vinci.server.feed.controller;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.feed.domain.FeedComments;
import im.vinci.server.feed.domain.wrapper.ListFeedCommentsResponse;
import im.vinci.server.feed.service.FeedCommentsService;
import im.vinci.server.security.ApiSecurityLabel;
import im.vinci.server.utils.UserContext;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import im.vinci.server.utils.apiresp.Result;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子评论接口
 * Created by ASUS on 2016/8/10.
 */
@RestController
@RequestMapping(value = "/vinci/feed/comment", produces = "application/json;charset=UTF-8")
public class FeedCommentsController {

    @Autowired
    private FeedCommentsService feedCommentsService;


    //发表Feed评论
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public Result publishComments(@RequestBody FeedComments feedComments) {
        feedComments.setUserId(UserContext.getUserInfo().getId());
        feedCommentsService.publishComments(feedComments);
        return new Result();
    }

    //删除Feed评论
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public Result deleteComments(@RequestParam("feed_id") long feedId, @RequestParam("comment_id") long commentId) {
        feedCommentsService.deleteComments(feedId, commentId);
        return new Result();
    }

    //Feed评论列表
    @RequestMapping(value = "/list")
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<ResponsePageVo<ListFeedCommentsResponse>> listCommentsAndUserInfo(@RequestParam("feed_id") long feedId,
                                                                                          @RequestParam(value = "last_comment_id", defaultValue = "0") long lastCommentId,
                                                                                          @RequestParam(value = "page_size", defaultValue = "20") int pageSize) {
        if (pageSize <= 0 || pageSize > 100) {
            throw new VinciException(ErrorCode.FEED_COMMENTS_LIST_PARAMETER_ERROR, "page或pageSize不合法", "参数不合法");
        }
        return new ResultObject<>(feedCommentsService.listCommentsAndUserInfo(feedId, lastCommentId, pageSize));
    }

}
