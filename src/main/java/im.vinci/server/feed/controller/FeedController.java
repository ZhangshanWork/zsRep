package im.vinci.server.feed.controller;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.feed.domain.Feed;
import im.vinci.server.feed.domain.FeedSearch;
import im.vinci.server.feed.domain.wrapper.PublishFeedRequest;
import im.vinci.server.feed.service.FeedService;
import im.vinci.server.security.ApiSecurityLabel;
import im.vinci.server.utils.apiresp.Result;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Feed接口
 * Created by frank on 16-8-5.
 */
@RestController
@RequestMapping(value = "/vinci/feed", produces = "application/json;charset=UTF-8")
public class FeedController {

    @Autowired
    FeedService feedService;



    //发表Feed,发表普通Feed
    @RequestMapping(value = "/publish" ,method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public Result publishFeed(@RequestBody Feed feed) {
        if (StringUtils.hasText(feed.getPageType())
                || StringUtils.hasText(feed.getPageContent())) {
            throw new VinciException(ErrorCode.FEED_PUBLISH_PAGE_TYPE_NEED_EMPTY,"不允许传入page type","参数不正确,只允许普通发布");
        }
        feed.setPageType("none");
        feed.setPageContent("");
        feedService.publishFeed(feed);
        return new Result();
    }

    //发表Feed,发表Music Share Feed
    @RequestMapping(value = "/{page_type}/publish" ,method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public Result publishMusicFeed(@RequestBody PublishFeedRequest feed,
                                   @PathVariable("page_type") String pageType) {
        feedService.publishFeed(pageType,feed);
        return new Result();
    }


    //删除Feed
    @RequestMapping(value = "/delete")
    @ApiSecurityLabel(isCheckLogin = true)
    public Result deleteFeed(@RequestParam("feedId") long feedId) {
        feedService.deleteFeed(feedId);
        return new Result();
    }

    /**
     * 返回关注的人的某搜索词下的帖子
     * param: ()搜索词
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<Object> UserMessageList(@RequestParam(value = "search_term", required = false) String searchTerm,
                                                @RequestParam(value = "page_type", required = false) String pageType,
                                                @RequestParam(value = "topic", required = false) String topic,
                                                //all 所有人  attention 关注的人 special 指定人
                                                @RequestParam(value = "attention_type", defaultValue = "all") String attentionType,
                                                //当attention type = special时,要看哪个人的帖子
                                                @RequestParam(value = "uid", defaultValue = "0") long uid,
                                                @RequestParam("offset") int offset,
                                                @RequestParam("page_size") int pageSize) {
        FeedSearch feedSearch = new FeedSearch();
        //feedSearch.setPageType(pageType).setTopic(topic).setUserId(1);
        feedSearch.setPageType(pageType).setTopic(topic).setAttentionType(attentionType).setUserId(uid);
        return new ResultObject<>(feedService.feedSearch(searchTerm, feedSearch, offset, pageSize));
    }
}
