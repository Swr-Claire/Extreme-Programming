package com.lantu.common.excel;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.context.xls.DefaultXlsReadContext;
import com.alibaba.excel.context.xlsx.DefaultXlsxReadContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.xls.XlsReadSheetHolder;
import com.alibaba.excel.read.metadata.holder.xlsx.XlsxReadSheetHolder;

import java.util.*;

/**
 * 直接用map接收数据
 *
 * @author hck
 */
public class NoModleDataListener extends AnalysisEventListener<Map<Integer, String>> {
    /**
     * 存放各个小表的表名和数据
     */
    LinkedHashMap<String, List<Map<Integer, String>>> sheetDataMapBySheetName = new LinkedHashMap<>();

    /**
     * 每一行数据都会执行一遍这个方法
     * @param data data
     * @param context context
     */
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        String sheetName;
        if(context instanceof DefaultXlsReadContext){
            DefaultXlsReadContext readContext = (DefaultXlsReadContext)context;
            XlsReadSheetHolder readHolder = (XlsReadSheetHolder) readContext.currentReadHolder();
            sheetName = readHolder.getSheetName();
        }else if(context instanceof DefaultXlsxReadContext){
            DefaultXlsxReadContext readContext = (DefaultXlsxReadContext)context;
            XlsxReadSheetHolder readHolder = (XlsxReadSheetHolder) readContext.currentReadHolder();
            sheetName = readHolder.getSheetName();
        }else return;
        List<Map<Integer, String>> dataList = sheetDataMapBySheetName.computeIfAbsent(sheetName.trim(), (key) -> {
            return new ArrayList<Map<Integer, String>>();
        });
        // 对字符串进行去空格操作
        for(Map.Entry<Integer,String> entry : data.entrySet()){
            if(null != entry.getValue()){
                data.put(entry.getKey(), StrUtil.trim(entry.getValue()));
            }
        }
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }


    /**
     * 获取导入的数据
     * @return
     */
    public LinkedHashMap<String, List<Map<Integer, String>>> getImportData() {
        return sheetDataMapBySheetName;
    }
}
