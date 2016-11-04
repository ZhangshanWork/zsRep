package im.vinci.server.search.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.naturelang.listener.Context;
import im.vinci.server.search.domain.himalayas.*;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.JsonUtils;
import im.vinci.server.utils.WebUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 封装了喜马拉雅的一些搜索接口
 * Created by tim@vinci on 15/12/23.
 */
@Service
public class HimalayaSearchService {

    private static enum RecommendTrackCondition {
        hot,
        daily,
        recent,
        favorite
    }

    Cache<String, GetHimalayaCategoryResponse> categoryResponseCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.HOURS)
            .maximumSize(10).build();
    Cache<Integer, GetHimalayaCategoryTagsResponse> categoryTagsResponseCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.HOURS)
            .maximumSize(100).build();

    /**
     * 通过id获取喜马拉雅的专辑信息包括里面的音乐
     *
     * @param id
     */
    public GetHimalayaAlbumDetailResponse getAlbumById(final long id, final int page, final int pageSize) {
        return new BizTemplate<GetHimalayaAlbumDetailResponse>("getHimalayaAlbumById") {
            private int iPage = page;
            private int iPageSize = pageSize;

            @Override
            protected void checkParams() throws VinciException {
                if (id <= 0) {
                    throw new VinciException(ErrorCode.HIMALAYA_ALBUM_NOT_EXIST, "himalaya(" + id + ") is not exist", "专辑不存在");
                }
                if (page <= 0) {
                    iPage = 1;
                }
                if (pageSize <= 0 || pageSize > 50) {
                    iPageSize = 10;
                }
            }

            @Override
            protected GetHimalayaAlbumDetailResponse process() throws Exception {
              /*  String url = "http://3rd.ximalaya.com/albums/" + id + "/tracks?i_am=vinci&page=" + iPage + "&per_page=" + iPageSize + "&is_asc=true&uni=xxx";
                GetHimalayaAlbumDetailResponse response = null;
                try {
                    String content = WebUtils.doGet(url, Collections.emptyMap());
                    response = JsonUtils.decode(content, GetHimalayaAlbumDetailResponse.class);
                } catch (Exception e) {
                    throw new VinciException(e, ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error", "服务器错误");
                }
                return checkResponse(response, GetHimalayaAlbumDetailResponse.class);*/
                GetHimalayaAlbumDetailResponse response = new GetHimalayaAlbumDetailResponse();
                response.setRet(0);
                response.setAlbum(Context.getOssHimalaya(id+""));
                //String content = JSON.toJSONString(response);
                //response = JsonUtils.decode(content, GetHimalayaAlbumDetailResponse.class);
                return checkResponse(response, GetHimalayaAlbumDetailResponse.class);
            }
        }.execute();
    }

    /**
     * 获取分类下推荐的专辑
     *
     * @param categoryId 大分类id
     * @param tag        分类下的标签，为null或空是表示从整个大分类推荐
     */
    public GetHimalayaRecommendAlbumInCategoryResponse recommendAlbumInCategory(final long categoryId, final String tag, final int page, final int pageSize) {
        return new BizTemplate<GetHimalayaRecommendAlbumInCategoryResponse>("getHimalayaAlbumByRecommendInCategory") {
            private int iPage = page;
            private int iPageSize = pageSize;

            @Override
            protected void checkParams() throws VinciException {
                if (categoryId < 0) {
                    throw new VinciException(ErrorCode.HIMALAYA_PARAM_ERROR, "himalaya(" + categoryId + ") is not exist", "专辑不存在");
                }
                if (page <= 0) {
                    iPage = 1;
                }
                if (pageSize <= 0 || pageSize > 50) {
                    iPageSize = 10;
                }
            }

            @Override
            protected GetHimalayaRecommendAlbumInCategoryResponse process() throws Exception {
                String url = "http://3rd.ximalaya.com/categories/" + categoryId + "/hot_albums";
                ImmutableMap.Builder<String, String> paramBuilder = ImmutableMap.<String, String>builder()
                        .put("i_am", "vinci").put("page", String.valueOf(iPage))
                        .put("per_page", String.valueOf(iPageSize))
                        .put("uni", "xxx");
                if (!StringUtils.isEmpty(tag)) {
                    paramBuilder.put("tag", tag);
                }
                GetHimalayaRecommendAlbumInCategoryResponse response;
                try {
                    String content = WebUtils.doGet(url, paramBuilder.build());
                    response = JsonUtils.decode(content, GetHimalayaRecommendAlbumInCategoryResponse.class);
                } catch (Exception e) {
                    throw new VinciException(e, ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error", "服务器错误");
                }
                return checkResponse(response, GetHimalayaRecommendAlbumInCategoryResponse.class);
            }
        }.execute();
    }

    /**
     * 获取分类下推荐的声音
     *
     * @param categoryId 大分类id
     * @param tag        分类下的标签，为null或空是表示从整个大分类推荐
     * @param condition  条件，从4个中选一
     */
    public GetHimalayaRecommendTrackInCategoryResponse recommendTracksInCategory(
            final long categoryId, final String tag,
            final String condition, final int page, final int pageSize) {
        return new BizTemplate<GetHimalayaRecommendTrackInCategoryResponse>("getHimalayaTracksByRecommendInCategory") {
            private int iPage = page;
            private int iPageSize = pageSize;
            private RecommendTrackCondition iCondition = RecommendTrackCondition.hot;
            @Override
            protected void checkParams() throws VinciException {
                if (categoryId < 0) {
                    throw new VinciException(ErrorCode.HIMALAYA_PARAM_ERROR, "himalaya(" + categoryId + ") is not exist", "专辑不存在");
                }
                if (page <= 0) {
                    iPage = 1;
                }
                if (pageSize <= 0 || pageSize > 50) {
                    iPageSize = 10;
                }
                try {
                    iCondition = RecommendTrackCondition.valueOf(condition);
                }catch (Exception e) {
                    //ignore
                }
            }

            @Override
            protected GetHimalayaRecommendTrackInCategoryResponse process() throws Exception {
                String url = "http://3rd.ximalaya.com/explore/tracks";
                ImmutableMap.Builder<String, String> paramBuilder = ImmutableMap.<String, String>builder()
                        .put("i_am", "vinci").put("page", String.valueOf(iPage))
                        .put("per_page", String.valueOf(iPageSize))
                        .put("uni", "xxx").put("category_id",String.valueOf(categoryId));
                if (!StringUtils.isEmpty(tag)) {
                    paramBuilder.put("tag", tag);
                }
                if (condition != null) {
                    paramBuilder.put("condition",iCondition.name());
                }
                GetHimalayaRecommendTrackInCategoryResponse response;
                try {
                    String content = WebUtils.doGet(url, paramBuilder.build());
                    response = JsonUtils.decode(content, GetHimalayaRecommendTrackInCategoryResponse.class);
                } catch (Exception e) {
                    throw new VinciException(e, ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error", "服务器错误");
                }
                return checkResponse(response, GetHimalayaRecommendTrackInCategoryResponse.class);
            }
        }.execute();
    }

    /**
     * 通过关键词搜索专辑
     *
     * @param categoryId 在那个分类下搜索，-1为在全部
     * @param keyword    搜索词
     * @param page       页码
     * @param pageSize   每页结果数量
     */
    public QueryHimalayaAlbumByKeywordResponse queryAlbumByKeyword(final int categoryId, final String keyword, final int page, final int pageSize) {
        return new BizTemplate<QueryHimalayaAlbumByKeywordResponse>("getHimalayaAlbumByKeyword") {
            private int iPage = page;
            private int iPageSize = pageSize;

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(keyword)) {
                    throw new VinciException(ErrorCode.HIMALAYA_PARAM_ERROR, "himalaya keyword is empty", "搜索词为空");
                }
                if (page <= 0) {
                    iPage = 1;
                }
                if (pageSize <= 0 || pageSize > 50) {
                    iPageSize = 10;
                }
            }

            @Override
            protected QueryHimalayaAlbumByKeywordResponse process() throws Exception {
                String url = "http://3rd.ximalaya.com/search/albums";
                ImmutableMap.Builder<String, String> paramBuilder = ImmutableMap.<String, String>builder()
                        .put("i_am", "vinci").put("page", String.valueOf(iPage))
                        .put("per_page", String.valueOf(iPageSize))
                        .put("uni", "xxx").put("q", keyword);
                if (categoryId >= 0) {
                    paramBuilder.put("category_id", String.valueOf(categoryId));
                }
                QueryHimalayaAlbumByKeywordResponse response;
                try {
                    String content = WebUtils.doGet(url, paramBuilder.build());
                    response = JsonUtils.decode(content, QueryHimalayaAlbumByKeywordResponse.class);
                } catch (Exception e) {
                    throw new VinciException(e, ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error", "服务器错误");
                }
                return checkResponse(response, QueryHimalayaAlbumByKeywordResponse.class);
            }
        }.execute();
    }
    
    /**
     * 通过关键词搜索声音
     *
     * @param categoryId 在那个分类下搜索，-1为在全部
     * @param keyword    搜索词
     * @param page       页码
     * @param pageSize   每页结果数量
     */
   public QueryHimalayaTrackByIdResponse queryTrackById(final Long trackId){
	   return new BizTemplate<QueryHimalayaTrackByIdResponse>("getHimalayaTrackById") {
           @Override
           protected void checkParams() throws VinciException {
               if (StringUtils.isEmpty(trackId)) {
                   throw new VinciException(ErrorCode.HIMALAYA_PARAM_ERROR, "himalaya id is empty", "文件id为空");
               }
           }

           @Override
           protected QueryHimalayaTrackByIdResponse process() throws Exception {
               String url = "http://3rd.ximalaya.com/tracks/"+trackId;
               ImmutableMap.Builder<String, String> paramBuilder = ImmutableMap.<String, String>builder()
                       .put("i_am", "vinci").put("uni", "xxx");
               QueryHimalayaTrackByIdResponse response;
               try {
                   String content = WebUtils.doGet(url, paramBuilder.build());
                   response = JsonUtils.decode(content, QueryHimalayaTrackByIdResponse.class);
               } catch (Exception e) {
                   throw new VinciException(e, ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error", "服务器错误");
               }
               return checkResponse(response, QueryHimalayaTrackByIdResponse.class);
           }
       }.execute();
   }
   
    /**
     * 通过关键词搜索声音
     *
     * @param categoryId 在那个分类下搜索，-1为在全部
     * @param keyword    搜索词
     * @param page       页码
     * @param pageSize   每页结果数量
     */
    public QueryHimalayaTrackByKeywordResponse queryTrackByKeyword(final int categoryId, final String keyword, final int page, final int pageSize) {
        return new BizTemplate<QueryHimalayaTrackByKeywordResponse>("getHimalayaAlbumByKeyword") {
            private int iPage = page;
            private int iPageSize = pageSize;

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(keyword)) {
                    throw new VinciException(ErrorCode.HIMALAYA_PARAM_ERROR, "himalaya keyword is empty", "搜索词为空");
                }
                if (page <= 0) {
                    iPage = 1;
                }
                if (pageSize <= 0 || pageSize > 50) {
                    iPageSize = 10;
                }
            }

            @Override
            protected QueryHimalayaTrackByKeywordResponse process() throws Exception {
                String url = "http://3rd.ximalaya.com/search/tracks";
                ImmutableMap.Builder<String, String> paramBuilder = ImmutableMap.<String, String>builder()
                        .put("i_am", "vinci").put("page", String.valueOf(iPage))
                        .put("per_page", String.valueOf(iPageSize))
                        .put("uni", "xxx").put("q", keyword);
                if (categoryId >= 0) {
                    paramBuilder.put("category_id", String.valueOf(categoryId));
                }
                QueryHimalayaTrackByKeywordResponse response;
                try {
                    String content = WebUtils.doGet(url, paramBuilder.build());
                    response = JsonUtils.decode(content, QueryHimalayaTrackByKeywordResponse.class);
                } catch (Exception e) {
                    throw new VinciException(e, ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error", "服务器错误");
                }
                return checkResponse(response, QueryHimalayaTrackByKeywordResponse.class);
            }
        }.execute();
    }

    /**
     * 获取喜马拉雅的分类
     */
    public GetHimalayaCategoryResponse getCategories() {
        return new BizTemplate<GetHimalayaCategoryResponse>("getHimalayaCategories") {
            @Override
            protected void checkParams() throws VinciException {
            }

            @Override
            protected GetHimalayaCategoryResponse process() throws Exception {

                String url = "http://3rd.ximalaya.com/categories?i_am=vinci&uni=xxx";
                GetHimalayaCategoryResponse response = categoryResponseCache.getIfPresent("key");
                if (response != null) {
                    return response;
                }
                try {
                    String content = WebUtils.doGet(url, Collections.emptyMap());
                    response = JsonUtils.decode(content, GetHimalayaCategoryResponse.class);
                } catch (Exception e) {
                    throw new VinciException(e, ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error", "服务器错误");
                }
                if (response != null) {
                    categoryResponseCache.put("key", response);
                }
                return checkResponse(response, GetHimalayaCategoryResponse.class);
            }
        }.execute();
    }

    /**
     * 获取喜马拉雅的分类下的标签
     */
    public GetHimalayaCategoryTagsResponse getCategories(final int categoryId) {
        return new BizTemplate<GetHimalayaCategoryTagsResponse>("getHimalayaCategoryTags") {
            @Override
            protected void checkParams() throws VinciException {
            }

            @Override
            protected GetHimalayaCategoryTagsResponse process() throws Exception {

                String url = "http://3rd.ximalaya.com/categories/" + categoryId + "/tags?i_am=vinci&uni=xxx";
                GetHimalayaCategoryTagsResponse response = categoryTagsResponseCache.getIfPresent(categoryId);
                if (response != null) {
                    return response;
                }
                try {
                    String content = WebUtils.doGet(url, Collections.emptyMap());
                    response = JsonUtils.decode(content, GetHimalayaCategoryTagsResponse.class);
                } catch (Exception e) {
                    throw new VinciException(e, ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error", "服务器错误");
                }
                if (response != null) {
                    categoryTagsResponseCache.put(categoryId, response);
                }
                return checkResponse(response, GetHimalayaCategoryTagsResponse.class);
            }
        }.execute();
    }

    private <T extends HimalayaBaseResponse> T checkResponse(HimalayaBaseResponse response, Class<T> clz) {
        if (response == null) {
            throw new VinciException(ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error: null", "服务器错误");
        }
        if (response.getRet() != 0) {
            throw new VinciException(ErrorCode.HIMALAYA_REMOTE_SERVER_ERROR, "remote server error:" + response.getRet() + "," + response.getErrmsg(), "服务器错误");
        }
        return (T) response;
    }

}
