package im.vinci.server.elasticsearch.domain.base;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhongzhengkai on 16/2/24.
 *
 *
 */
public abstract class BasicDocModel {

    private Map<String,Object> docMap = new HashMap<>();

    private XContentBuilder builder = null;

    public BasicDocModel() throws IOException{
        initDocMap(docMap);
        builder = XContentFactory.jsonBuilder();
        builder.map(docMap);
    }

    public static abstract class ReInitMapAction{
        public ReInitMapAction(){
        }
        public abstract void reInit(Map<String,Object> docMapRef);
    }

    public static abstract class ReInitDocXContentAction{
        public ReInitDocXContentAction(){
        }

        /**
         * 多种写法操作builderRef构造XContentBuilder对象
         * [写法1]------------------------------------
         * builderRef.startObject()
         * builderRef.field($key,$value)
         * ......
         * buiderRef.endObject()
         * [写法2]------------------------------------
         * buiderRef.map($map)
         * [写法3]------------------------------------
         * buiderRef.startArray
         * ......
         * buiderRef.endArray
         * @param builderRef
         * @throws IOException
         */
        public abstract void reInit(XContentBuilder builderRef) throws IOException;
    }

    //实现这个文档对象所对应的map对象,系统会自动生成这个map对象所对应的XContentBuilder对象(方便提交给esClient)
    public abstract void initDocMap(Map<String,Object> map);

    //这个是提交给esclient的builder对象
    public XContentBuilder getXContentBuilder(){
        return builder;
    }

    public Object getDocValue(String fieldName){
        return getDocAsMap().get(fieldName);
    }

    public Map<String,Object> getDocAsMap(){
        return docMap;
    }

    /**
     * 重新初始化该文档的map对象,注意,其对应的builder对象也会跟着改变
     * @param action
     * @return
     * @throws IOException
     */
    public XContentBuilder reInitDocMap(ReInitMapAction action) throws IOException {
        docMap.clear();
        action.reInit(docMap);
        builder = XContentFactory.jsonBuilder();
        builder.map(docMap);
        return builder;
    }

    /**
     * 重新初始化一个XContentBuilder并返回,该方法仅仅改变文档的builder对象,其map对象还是原值
     * 如果需要map对象与builder对象同时改变,请调用reInitDocMap()
     * ------------------------------------------------------------------------
     * 这里有个坑!!!! builder.string()一旦在外界调用,那么这里再次builder.startObject()会报空指针异常
     * (builder.string()里面调用了close(),可以人为调用builder.close()验证该错误),所以构造该
     * 函数时重新构造一个XContentBuilder对象并返回给外界,这样比较安全
     *
     */
    public XContentBuilder reInitDocXContentBuilder(ReInitDocXContentAction action) throws IOException{
        builder = XContentFactory.jsonBuilder();
        action.reInit(builder);
        return this.builder;

    }
}
