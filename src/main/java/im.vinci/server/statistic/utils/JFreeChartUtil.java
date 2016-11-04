package im.vinci.server.statistic.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.awt.*;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zhongzhengkai on 16/1/7.
 */
public class JFreeChartUtil {

//    //避免中文主题出现乱码
//    static{
//        //创建主题样式
//        StandardChartTheme standardChartTheme=new StandardChartTheme("CN");
//        //设置标题字体
//        standardChartTheme.setExtraLargeFont(new Font("隶书",Font.BOLD,20));
//        //设置图例的字体
//        standardChartTheme.setRegularFont(new Font("宋书",Font.PLAIN,15));
//        //设置轴向的字体
//        standardChartTheme.setLargeFont(new Font("宋书",Font.PLAIN,15));
//        //应用主题样式
//        ChartFactory.setChartTheme(standardChartTheme);
//    }

    //折线图
    public static String drawLineChart(Map<String, ArrayList<DotData>> dataSource,String titleStr, String xTag, String yTag, String picName) throws Exception {
        if (dataSource.size() == 0) return null;
        JFreeChart chart = ChartFactory.createLineChart(titleStr, // 标题
                xTag, // categoryAxisLabel （category轴，横轴，X轴标签）
                yTag, // valueAxisLabel（value轴，纵轴，Y轴的标签）
                buildLineOrBarDataset(dataSource), // dataset
                PlotOrientation.VERTICAL,
                true, // 是否显示标题
                true, // tooltips
                false); // URLs

//        TextTitle title = new TextTitle(titleStr, new Font("宋体", Font.BOLD, 20));
//        // 解决曲线图片标题中文乱码问题
//        chart.setTitle(title);
//        chart.getTitle().setFont(new Font("黑体",Font.BOLD,20));//设置标题字体

        _configChartPlot(chart);

        // 使用CategoryPlot设置各种参数。以下设置可以省略。
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        // 背景色 透明度
        plot.setBackgroundAlpha(0.5f);
        // 前景色 透明度
        plot.setForegroundAlpha(1.0f);
        // 其他设置 参考 CategoryPlot类
        return _saveAsJPG(chart,picName);
    }

    //平面饼图
    public static String drawPieChart(Map<String,Integer> dataSource, String titleStr, String picName) throws Exception{
        if (dataSource.size() == 0) return null;
        JFreeChart chart = ChartFactory.createPieChart(titleStr, buildPieDataset(dataSource), true, true, false);
        //3d饼图
//      chart = ChartFactory.createPieChart3D("", createDataset(), true, true, false);

//        TextTitle title = new TextTitle(titleStr, new Font("宋体", Font.BOLD, 20));
//        // 解决曲线图片标题中文乱码问题
//        chart.setTitle(title);

        _configChartPlot(chart);

        //通过JFreeChart 对象获得 plot：PiePlot！！
        PiePlot pieplot = (PiePlot)chart.getPlot();
        // 没有数据的时候显示的内容
        pieplot.setNoDataMessage("No data available");
        // 设置无数据时的信息显示颜色
        pieplot.setNoDataMessagePaint(Color.red);
        //把Lable 为”Two” 的那一块”挖”出来10%
        pieplot.setExplodePercent("Two", 0.3D);
        //设置背景透明度
        pieplot.setBackgroundAlpha(0.5f);
        //设置前景透明度
        pieplot.setForegroundAlpha(0.6f);
        // 指定饼图轮廓线的颜色
        pieplot.setBaseSectionOutlinePaint(Color.white);
        pieplot.setBaseSectionPaint(Color.BLACK);
        // 指定显示的饼图上圆形(true)还椭圆形(false)
        pieplot.setCircular(true);
        //("{0}: ({1}，{2})")是生成的格式，
        //{0}表示数据名，{1}表示数据的值，{2}表示百分比。可以自定义。
        //而new DecimalFormat("0.00%")表示小数点后保留两位。
        pieplot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                ("{0}({1}):{2}"), NumberFormat.getNumberInstance(),
                new DecimalFormat("0.00%")));
        return _saveAsJPG(chart, picName);
    }

    //柱状图
    public static String drawBarChart(Map<String, ArrayList<DotData>> dataSource, String titleStr, String xTag, String yTag, String picName) throws Exception{
        if (dataSource.size() == 0) return null;



//        JFreeChart chart = ChartFactory.createBarChart3D(//画3d图
        JFreeChart chart = ChartFactory.createBarChart(
                titleStr, // 图表标题
                xTag, // 目录轴的显示标签
                yTag, // 数值轴的显示标签
                buildLineOrBarDataset(dataSource), // 数据集
                PlotOrientation.VERTICAL, // 图表方向：水平、垂直
                true,  // 是否显示图例(对于简单的柱状图必须是 false)
                false, // 是否生成工具
                false  // 是否生成 URL 链接
        );

        //中文乱码
//        TextTitle textTitle = chart.getTitle();
//        textTitle.setFont(new Font("黑体", Font.PLAIN, 20));

        _configChartPlot(chart);
        return _saveAsJPG(chart, picName);
    }

    //调用此方法,设置chart的视图对象plot各种属性
    private static void _configChartPlot(JFreeChart chart){
        // 配置字体,解决中文乱码问题
        Font xfont = new Font("宋体",Font.PLAIN,14) ;// X轴
        Font yfont = new Font("宋体",Font.PLAIN,12) ;// Y轴
        Font kfont = new Font("宋体",Font.PLAIN,14) ;// 底部
        Font titleFont = new Font("隶书", Font.BOLD , 25) ; // 图片标题
        Plot plot = chart.getPlot();// 图形的绘制结构对象

        // 图片标题
        chart.setTitle(new TextTitle(chart.getTitle().getText(),titleFont));
        // 底部
        chart.getLegend().setItemFont(kfont);

        //饼状图的chart对象是PiePlot类型的,无需进行以下设置
        if (plot instanceof CategoryPlot) {
            CategoryPlot categoryPlot = (CategoryPlot) plot;
            // X 轴
            CategoryAxis domainAxis = categoryPlot.getDomainAxis();
            domainAxis.setLabelFont(xfont);// 轴标题
            domainAxis.setTickLabelFont(xfont);// 轴数值
            domainAxis.setTickLabelPaint(Color.BLUE); // 字体颜色
//            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // 横轴上的label向左倾斜45度显示
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD); // 标准显示

            // Y 轴
            ValueAxis rangeAxis = categoryPlot.getRangeAxis();
            rangeAxis.setLabelFont(yfont);
            rangeAxis.setLabelPaint(Color.BLUE); // 字体颜色
            rangeAxis.setTickLabelFont(yfont);

            //网格线设置
            categoryPlot.setDomainGridlinesVisible(true);//是否显示纵向格子线
//            categoryPlot.setDomainGridlinePaint(Color.black);
            categoryPlot.setRangeGridlinesVisible(true); //是否显示水平网格线
//            categoryPlot.setRangeGridlinePaint(Color.black);//设置水平网格线颜色

            //设置显示x抽对应的Y抽数据
            LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) ((CategoryPlot)plot).getRenderer();// 数据点
            lineandshaperenderer.setBaseShapesVisible(true);// series 点（即数据点）可见
            lineandshaperenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator()); // 显示数据点的数据
            lineandshaperenderer.setBaseItemLabelsVisible(true);  // 显示折线图点上的数据
        }else if(plot instanceof XYPlot){
            //设置显示x抽对应的Y抽数据
            XYPlot xyPlot = chart.getXYPlot();
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
            renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
            renderer.setBaseItemLabelsVisible(true);
        }

    }

    private static String _saveAsJPG(JFreeChart chart, String picName) throws Exception{
        FileOutputStream out = null;
        String fullName = "";
        try {
            //把dir设置到src下儿不是build目录下,
            //是为了防止每次执行gradle clean build命令,把那些生成的图片删掉了

            //使用了java运行时的系统属性来得到jar文件的位置,如线上运行时可获得:/opt/develop/vinci/vinci_statistic/build/libs/vinci_statistic-1.2.0.jar
            String curPath = System.getProperty("java.class.path");
            curPath = curPath.substring(0,curPath.lastIndexOf("/")+1);//得到:/opt/develop/vinci/vinci_statistic/build/libs/



            //得到:file:/opt/develop/vinci/vinci_statistic/build/libs/vinci_statistic-1.2.0.jar!/
//            String curPath = JFreeChartUtil.class.getResource("/").getPath();
//            if(curPath.startsWith("file")){
//                curPath = curPath.split(":")[1];
//                System.out.println("now the curPath is:"+curPath);
//                if(curPath.endsWith(".jar")){
//                    curPath = curPath.substring(0,curPath.lastIndexOf("/")+1);
//                }else if(curPath.indexOf(".jar")!= -1 && curPath.endsWith("/")){
//                    //把/opt/develop/vinci/vinci_statistic/build/libs/vinci_statistic-1.2.0.jar!/
//                    //变为:/opt/develop/vinci/vinci_statistic/build/libs/
//                    String subPath = curPath.substring(0, curPath.lastIndexOf("/"));//截掉最后一个 /
//                    curPath = subPath.substring(0, subPath.lastIndexOf("/")+1);//截掉最后一个/之后的内容
//                }
//            }

            String picDir = curPath + "../../stats_image/";
//            File outFile = new File(outputPath);
//            if (!outFile.getParentFile().exists()) {
//                outFile.getParentFile().mkdirs();
//            }
            String picPath = picDir + picName + ".jpg";
            System.out.println("picture path is " + picPath);
            out = new FileOutputStream(picPath);
            fullName = picPath;
            ChartUtilities.writeChartAsJPEG(out, 1.0f,chart,1200,800,null);
        } finally {
            try {
                out.close();
            } catch (Exception e) {}
            return fullName;
        }
    }

    //构造饼状图源数据集
    private static PieDataset buildPieDataset(Map<String, Integer> dataSource) {
        Iterator it = dataSource.entrySet().iterator();
        DefaultPieDataset pDataset = new DefaultPieDataset();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry) it.next();
            pDataset.setValue(entry.getKey(), entry.getValue());
        }
        return pDataset;
    }

    private static CategoryDataset buildLineOrBarDataset(Map<String, ArrayList<DotData>> dataSource) {
        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
        Iterator it = dataSource.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ArrayList<DotData>> entry = (Map.Entry) it.next();
            ArrayList<DotData> list = entry.getValue();
            String rowKey = entry.getKey();
            for (DotData dot : list) {
                categoryDataset.addValue(dot.value, rowKey, dot.name);
            }
        }
        return categoryDataset;
    }


    public static class DotData {
        public long value;
        public String name;

        public DotData(String name, long value) {
            this.value = value;
            this.name = name;
        }
    }

//    public static void main(String[] args){
//        String path = "/opt/develop/vinci/vinci_statistic/build/libs/vinci_statistic-1.2.0.jar!/";
//        String subPath = path.substring(0,path.lastIndexOf("/"));
//        System.out.println(subPath.substring(0,subPath.lastIndexOf("/")));
//
//        String classPath = System.getProperty("java.class.path");
//        System.out.println("classPath is:"+classPath);
//        URL url = new VinciApplication().getClass().getProtectionDomain().getCodeSource().getLocation();
//        System.out.println(url.getPath());
//    }
}
