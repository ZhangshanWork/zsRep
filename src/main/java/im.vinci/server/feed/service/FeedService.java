package im.vinci.server.feed.service;

import com.aliyun.opensearch.CloudsearchClient;
import com.aliyun.opensearch.CloudsearchSearch;
import com.aliyun.opensearch.object.KeyTypeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.feed.domain.Feed;
import im.vinci.server.feed.domain.FeedSearch;
import im.vinci.server.feed.domain.feeds.MusicFeedContent;
import im.vinci.server.feed.domain.wrapper.FeedSearchResponse;
import im.vinci.server.feed.domain.wrapper.PublishFeedRequest;
import im.vinci.server.feed.persistence.FeedMapper;
import im.vinci.server.user.domain.UserInfo;
import im.vinci.server.user.service.UserAttentionService;
import im.vinci.server.user.service.UserCountService;
import im.vinci.server.user.service.UserLoginAndBindDeviceService;
import im.vinci.server.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * feedService
 * Created by frank on 16-8-5.
 */
@Service
public class FeedService {

    @Autowired
    private FeedMapper feedMapper;

    @Autowired
    private UserCountService userCountService;

    @Autowired
    private UserLoginAndBindDeviceService userService;

    @Autowired
    private UserAttentionService userAttentionService;

    @Autowired
    private Environment env;

    private CloudsearchClient client;

    private String appName;

    @PostConstruct
    public void init() throws UnknownHostException {
        String accesskey = env.getProperty("mobile.feed_search.accessKeyId");
        String secret = env.getProperty("mobile.feed_search.accessKeySecret");
        appName = env.getProperty("mobile.feed_search.appName");
        String host = env.getProperty("mobile.feed_search.host");
        //阿里云搜索服务
        Map<String, Object> opts = new HashMap<>();
        client = new CloudsearchClient(accesskey, secret, host, opts, KeyTypeEnum.ALIYUN);
    }

    /**
     * 发表Feed
     */
    @Transactional
    public boolean publishFeed(final Feed feed){
        return new BizTemplate<Boolean>("feed.publish"){

            @Override
            protected void checkParams() throws VinciException{
                if (feed == null) {
                    throw new VinciException(ErrorCode.FEED_PUBLISH_ERROR, "feed为空", "没有传入任何内容");
                }
                if (feed.getContent() == null) {
                    feed.setContent("");
                }
                if (feed.getTopic() == null) {
                    feed.setTopic("");
                }
                int length = StringContentUtils.countRealLength(feed.getContent());
                if ((length<=0 && !"none".equals(feed.getPageType())) || length > 240) {
                    throw new VinciException(ErrorCode.FEED_PUBLISH_CONTENT_UNMATCH_RULE,"长度不符合规则","内容长度不符合规则");
                }
                if (StringContentUtils.hasUnshowChar(feed.getContent())) {
                    throw new VinciException(ErrorCode.FEED_COMMENTS_CONTENT_UNMATCH_RULE,"内容中有非显示字符","内容有不符合规则字符");
                }
                feed.setUserId(UserContext.getUserInfo().getId());
            }

            @Override
            protected Boolean process() throws VinciException{
                feed.setFeedId(LocalIdGenerator.INSTANCE.generateId());
                feedMapper.publishFeed(feed);
                feedMapper.insertCount(feed.getFeedId());
                userCountService.adjustFeedCount(UserContext.getUserInfo().getId(),false);
                return true;
            }
        }.execute();
    }
    /**
     * 发表特殊Feed
     */
    @Transactional
    public boolean publishFeed(final String pageType, final PublishFeedRequest feedRequest){
        return new BizTemplate<Boolean>("feed.publish_special_feed"){

            @Override
            protected void checkParams() throws VinciException{
                if (feedRequest == null) {
                    throw new VinciException(ErrorCode.FEED_PUBLISH_ERROR, "feed为空", "没有传入任何内容");
                }
            }

            @Override
            protected Boolean process() throws VinciException{
                Feed feed = new Feed();
                feed.setContent(feedRequest.getContent());
                feed.setTopic(feedRequest.getTopic());
                feed.setPageType(pageType);
                feed.setDtCreate(new Date());
                switch (pageType) {
                    case "music":
                        feed.setPageContent(JsonUtils.encode(checkMusicFeed(feedRequest.getPageContent())));
                        break;
                    default:
                        throw new VinciException(ErrorCode.FEED_PUBLISH_PAGE_TYPE_UNSUPPORTED,"不支持的feed类型:"+pageType,"该类型不支持");
                }
                return publishFeed(feed);
            }
        }.execute();
    }

    private MusicFeedContent checkMusicFeed(JsonNode pageContent) {
        MusicFeedContent content = JsonUtils.decode(pageContent.toString(),MusicFeedContent.class);
        if (content == null ) {
            throw new VinciException(ErrorCode.FEED_PUBLISH_PAGE_TYPE_ARGUMENT_ERROR, "conent is null", "没有传入音乐内容");
        }
        if (StringUtils.isEmpty(content.getSongName()) || StringUtils.isEmpty(content.getSongType()) ||
                content.getSongId() <=0) {
            throw new VinciException(ErrorCode.FEED_PUBLISH_PAGE_TYPE_ARGUMENT_ERROR, "音乐内容不完整", "音乐内容不存在");
        }
        if (StringUtils.isEmpty(content.getAlbumLogo())) {
            content.setAlbumLogo("http://www.vinci.im/static/images/Others/weixin_service.png");
        }
        return content;
    }

    /**
     * 删除Feed
     */
    @Transactional
    public boolean deleteFeed(final long feedId){
        return new BizTemplate<Boolean>("feed.delete"){

            @Override
            protected void checkParams() throws VinciException{
                if(feedId == 0){
                    throw new VinciException(ErrorCode.FEED_DELETE_NO_FEED_ID_ERROR, "传入feed为空或不合法", "没有选择FEED或不合法");
                }
            }

            @Override
            protected Boolean process() throws VinciException {
                Long userId  = feedMapper.getFeedUserById(feedId);
                if (userId == null) {
                    throw new VinciException(ErrorCode.FEED_DELETE_NOT_EXIST_ERROR, "删除Feed不存在", "该Feed不存在");
                }
                if (userId != UserContext.getUserInfo().getId()) {
                    throw new VinciException(ErrorCode.FEED_DELETE_FEED_ID_ERROR, "删除非本人FEED", "删除非本人FEED");
                }
                if (feedMapper.deleteFeed(feedId) == 0) {
                    throw new VinciException(ErrorCode.FEED_DELETE_NOT_EXIST_ERROR, "删除Feed不存在", "该Feed不存在");
                }
                userCountService.adjustFeedCount(UserContext.getUserInfo().getId(),true);
                return true;
            }
        }.execute();
    }

    public boolean isFeedExist(final long feedId) {
        Long userId  = feedMapper.getFeedUserById(feedId);
        return userId != null;
    }

    public Map<Long,Feed> getFeedByIds(final Collection<Long> feedIds) {
        return new BizTemplate<Map<Long,Feed>>("FeedService.getFeedByIds") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Map<Long,Feed> process() throws Exception {
                if (CollectionUtils.isEmpty(feedIds)) {
                    return Collections.emptyMap();
                }
                Map<Long,Feed> feedMap = feedMapper.getFeedByIds(feedIds);

                Set<Long> userIds = Sets.newHashSetWithExpectedSize(feedMap.size());
                feedMap.values().stream().forEach(feed -> userIds.add(feed.getUserId()));

                Map<Long,UserInfo> userInfoMap  = userService.getUserInfoMap(userIds);
                if (UserContext.getUserInfo() != null) {
                    userAttentionService.checkIsAttention(UserContext.getUserInfo().getId(),userInfoMap.values());
                }

                feedMap.values().stream().forEach(feed -> feed.setUserInfo(userInfoMap.get(feed.getUserId())));
                return feedMap;
            }
        }.execute();
    }

    /**
     * default:'好听' AND u:'3'|'1'
     *
     * @return 返回关注的人的帖子
     */
    public FeedSearchResponse feedSearch(final String searchTerm, final FeedSearch feedSearch, final int offset, final int pageSize) {
        return new BizTemplate<FeedSearchResponse>("feedSearch") {

            @Override
            protected void checkParams() throws VinciException {
                if (offset < 0 || pageSize < 0 || pageSize > 50) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "页面参数错误", "页面参数错误");
                }
            }

            @Override
            protected FeedSearchResponse process() throws VinciException {

                CloudsearchSearch search = new CloudsearchSearch(client);
                StringBuilder queryString = new StringBuilder("is_delete:'0'");
                if (StringUtils.hasText(searchTerm)) {
                    queryString.append("AND default:'").append(searchTerm).append("'");
                    search.setQueryString(searchTerm);
                }
                Set<Long> attentionList = Sets.newHashSet(userAttentionService.getUserAttentionListOnly(UserContext.getUserInfo().getId()));

                //用户过滤器字段
                if ("attention".equals(feedSearch.getAttentionType())) {
                    queryString.append(" AND u:");
                    for (long uid : attentionList) {
                        queryString.append("'").append(uid).append("'|");
                    }
                    queryString.deleteCharAt(queryString.length() - 1);
                } else if ("special".equals(feedSearch.getAttentionType())) {
                    queryString.append(" AND ").append("u:'").append(feedSearch.getUserId()).append("'");
                }

                // 设定过滤条件(PageType)
                if (!StringUtils.isEmpty(feedSearch.getPageType())) {
                    queryString.append(" AND page_type:'").append(feedSearch.getPageType()).append("'");
                }

                // 设定过滤条件()
                if (!StringUtils.isEmpty(feedSearch.getTopic())) {
                    queryString.append(" AND topic:'").append(feedSearch.getTopic()).append("'");
                }

                search.setQueryString(queryString.toString());

                // 添加指定搜索的应用：
                search.addIndex(appName);

                // 指定搜索返回的格式。
                search.setFormat("json");

                //按照时间倒叙
                search.addSort("feed_create", "-");

                //设定搜索结果集偏移量
                search.setStartHit(offset);
                //设定搜索结果集个数
                search.setHits(pageSize);

                // 返回搜索结果
                String searchResp = null;
                if (logger.isDebugEnabled()) {
                    logger.debug("search query:{}",queryString);
                }
                try {
                    searchResp = search.search();
                } catch (IOException e) {
                    throw new VinciException(e, ErrorCode.INTERNAL_SERVER_ERROR, "搜索出错:" + e.getMessage(), "内部错误");
                }
                try {
                    return parseSearchResult(searchResp, offset, pageSize,attentionList);
                } catch (VinciException e) {
                    throw e;
                } catch (Exception e) {
                    throw new VinciException(e, ErrorCode.INTERNAL_SERVER_ERROR, "搜索出错:" + searchResp, "内部错误");
                }
            }

        }.execute();
    }

    private FeedSearchResponse parseSearchResult(String searchResult, final int offset, final int pageSize, Set<Long> attentionList) {
        JsonNode rootNode = JsonUtils.decode(searchResult, JsonNode.class);
        if (rootNode == null) {
            throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR, "搜索出错:返回值为null", "内部错误");
        }
        JsonNode respStatus = rootNode.findValue("status");
        if (respStatus == null || !respStatus.isTextual() || !"OK".equals(respStatus.asText())) {
            throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR, "搜索出错,返回值:" + searchResult, "内部错误");
        }
        FeedSearchResponse resp = new FeedSearchResponse();
        JsonNode result = rootNode.get("result");
        int viewtotal = result.get("viewtotal").intValue();
        if (offset / pageSize >= 99 || offset + pageSize > viewtotal) {
            resp.setHasMore(false);
        } else {
            resp.setHasMore(true);
        }
        JsonNode items = result.get("items");
        if (items == null || !items.isArray()) {
            throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR, "搜索出错item为空,返回值:" + searchResult, "内部错误");
        }
//
        List<Long> feedIdList = Lists.newArrayListWithCapacity(items.size());
        items.forEach(node -> feedIdList.add(node.findPath("feed_id").asLong()));

        Map<Long,Feed> feedMap = getFeedByIds(feedIdList);


        List<Feed> feedList = Lists.newArrayListWithCapacity(items.size());
        resp.setFeeds(feedList);

        for (long feedId : feedIdList) {
            if (feedMap.containsKey(feedId)) {
                feedList.add(feedMap.get(feedId));
            }
        }

        return resp;
    }

    private Feed parseSearchItem(JsonNode item, Set<Long> attentionList) {
        Feed feed = new Feed();
//        UserInfo userInfo = new UserInfo();
//        UserCounts userCounts = new UserCounts();
//        feed.setUserInfo(userInfo);
//        userInfo.setUserCounts(userCounts);

        feed.setFeedId(item.findPath("feed_id").asLong());
        feed.setUserId(item.findPath("user_id").asLong());
        feed.setContent(item.findPath("content").asText());
        feed.setTopic(item.findPath("topic").asText());
        feed.setPageType(item.findPath("page_type").asText());
        feed.setPageContent(item.findPath("page_content").asText());
        feed.setDeleted(item.findPath("is_deleted").asBoolean());
        feed.setDtCreate(new Date(item.findPath("feed_create").asLong()));
        feed.setCommentCount(item.findPath("comment_count").asInt(0));

//        userInfo.setId(item.findPath("user_id").asLong());
//        userInfo.setNickName(item.findPath("nick_name").asText());
//        userInfo.setLocation(JsonUtils.decode(item.findPath("location").asText(), UserLocation.class));
//        userInfo.setSex(item.findPath("sex").asInt());
//        userInfo.setBirthDate(item.findPath("birth_date").asText());
//        userInfo.setHeadImg(item.findPath("head_img").asText());
//        userInfo.setDtCreate(new Date(item.findPath("user_create").asLong()));
//        userInfo.setAttention(attentionList.contains(userInfo.getId()));
//
//        userCounts.setUserId(userInfo.getId());
//        userCounts.setFeedCount(item.findPath("feed_count").asInt(0));
//        userCounts.setFollowerCount(item.findPath("follower_count").asInt(0));
//        userCounts.setAttentionerCount(item.findPath("attentioner_count").asInt(0));
//        userCounts.setCollectionCount(item.findPath("collection_count").asInt(0));

        return feed;
    }
    public static void main(String[] args) {
        String json = "{\"status\":\"OK\",\"request_id\":\"147278894117779943113487\",\"result\":{\"searchtime\":0.00779,\"total\":1,\"num\":1,\"viewtotal\":1,\"items\":[{\"feed_id\":\"104600213959531386\",\"user_id\":\"1\",\"content\":\"这首歌写的不错，很好听#好听#\",\"topic\":\"好听\",\"page_type\":\"SHARE_MUSIC\",\"page_content\":\"{\\\"album_id\\\":\\\"0\\\",\\\"album_logo\\\":\\\"\\\",\\\"album_name\\\":\\\"天台 电影原声带\\\",\\\"artist\\\":\\\"周杰伦\\\",\\\"artist_id\\\":\\\"6845\\\",\\\"listen_file\\\":\\\"http://english-listen-resource.oss-cn-beijing.aliyuncs.com/%E5%91%A8%E6%9D%B0%E4%BC%A6%2F%E5%91%A8%E6%9D%B0%E4%BC%A6-%E5%A4%A9%E5%8F%B0.mp3\\\",\\\"lyric_file\\\":\\\"\\\",\\\"play_counts\\\":666666,\\\"play_seconds\\\":106,\\\"singers\\\":\\\"周杰伦\\\",\\\"song_id\\\":1010160065,\\\"song_name\\\":\\\"天台\\\",\\\"song_type\\\":\\\"xiami\\\",\\\"sound_equalizer\\\":0,\\\"tag_counts\\\":[3],\\\"tags\\\":[\\\"周杰伦\\\"]}\",\"is_deleted\":\"0\",\"feed_create\":\"1472612395000\",\"nick_name\":\"周生明\",\"location\":\"{}\",\"sex\":\"1\",\"user_create\":\"1472557543000\",\"birth_date\":\"2016-08-30\",\"head_img\":\"http://inspero-images.img-cn-beijing.aliyuncs.com/head_img/1/0/1/104600207117573772.jpg@!mobileapp_headimg_640\",\"index_name\":\"stage_feed_search\"}],\"facet\":[]}}";
        System.out.println(JsonUtils.encode(new FeedService().parseSearchResult(json, 0, 10, Sets.newHashSet(1L))));
    }

}
