package im.vinci.server.search.controller;

import im.vinci.server.search.domain.himalayas.*;
import im.vinci.server.search.service.HimalayaSearchService;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 喜马拉雅各种搜索接口
 * Created by tim@vinci on 15/12/23.
 */
@RestController
@RequestMapping(value = {"/vinci/search/himalayas"}, produces = "application/json;charset=UTF-8")
public class HimalayaSearchController {

    @Autowired
    private HimalayaSearchService himalayaSearchService;

    /**
     * 获取专辑详细信息包括里面的资源文件
     */
    @RequestMapping(value = "album/{album_id}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultObject<GetHimalayaAlbumDetailResponse> getAlbumDetail(@PathVariable("album_id") long albumId,
                                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                                       @RequestParam(value = "page_size", defaultValue = "10") int pageSize) {

        return new ResultObject<>(himalayaSearchService.getAlbumById(albumId, page, pageSize));
    }

    /**
     * 通过id获取track文件
     */
    @RequestMapping(value = "track/{track_id}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultObject<QueryHimalayaTrackByIdResponse> getTrackById(@PathVariable("track_id") long trackId){
    	return new ResultObject<>(himalayaSearchService.queryTrackById(trackId));
    }
    
    /**
     * 获取一个分类下的推荐专辑
     */
    @RequestMapping(value = "category/{category_id}/albums")
    public ResultObject<GetHimalayaRecommendAlbumInCategoryResponse>
    recommendAlbumInCategory(@PathVariable("category_id") int categoryId,
                             @RequestParam(value = "tag", defaultValue = "") String tag,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             @RequestParam(value = "page_size", defaultValue = "10") int pageSize) {

        return new ResultObject<>(himalayaSearchService.recommendAlbumInCategory(categoryId, tag, page, pageSize));
    }

    /**
     * 获取一个分类下的推荐声音
     */
    @RequestMapping(value = "category/{category_id}/tracks")
    public ResultObject<GetHimalayaRecommendTrackInCategoryResponse>
    recommendAlbumInCategory(@PathVariable("category_id") int categoryId,
                             @RequestParam(value = "tag", defaultValue = "") String tag,
                             @RequestParam(value = "condition", defaultValue = "hot") String condition,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             @RequestParam(value = "page_size", defaultValue = "10") int pageSize) {
        return new ResultObject<>(himalayaSearchService.recommendTracksInCategory(categoryId, tag, condition, page, pageSize));
    }

    /**
     * 通过关键词搜索专辑
     */
    @RequestMapping(value = "search/albums")
    public ResultObject<QueryHimalayaAlbumByKeywordResponse>
    queryAlbumByKeyword(@RequestParam(value = "category_id", defaultValue = "-1") int categoryId,
                        @RequestParam(value = "key", defaultValue = "") String key,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "page_size", defaultValue = "10") int pageSize) {
        return new ResultObject<>(himalayaSearchService.queryAlbumByKeyword(categoryId, key, page, pageSize));
    }

    /**
     * 通过关键词搜索声音文件
     */
    @RequestMapping(value = "search/tracks")
    public ResultObject<QueryHimalayaTrackByKeywordResponse>
    queryTrackByKeyword(@RequestParam(value = "category_id", defaultValue = "-1") int categoryId,
                        @RequestParam(value = "key", defaultValue = "") String key,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "page_size", defaultValue = "10") int pageSize) {
        return new ResultObject<>(himalayaSearchService.queryTrackByKeyword(categoryId, key, page, pageSize));
    }

    @RequestMapping(value = "categories", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultObject<GetHimalayaCategoryResponse> getCategories() {
        return new ResultObject<>(himalayaSearchService.getCategories());
    }

    @RequestMapping(value = "category/{category_id}/tags")
    public ResultObject<GetHimalayaCategoryTagsResponse> getCategoryTags(@PathVariable("category_id") int categoryId) {
        return new ResultObject<>(himalayaSearchService.getCategories(categoryId));
    }


}
