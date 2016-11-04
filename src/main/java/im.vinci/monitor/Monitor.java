
package im.vinci.monitor;

/**
 * @author  sunli
 */
public interface Monitor {
    /**
     * 获取监控对象的名称
     * @return
     */
    public String getInstanceName();
    /**
     * 设置监控对象的描述信息
     * @param  description
     */
    public void setDescription(String description) ;
    /**
     * 获取监控對象的监控描述信息
     * @return
     */
    public String getDescription();
    /**
     * 获取监控對象的监控数据
     * @return
     */
    public Number getValue();

}
