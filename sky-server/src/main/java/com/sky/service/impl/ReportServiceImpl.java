package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ran Lewis
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // 用于存放所有日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin", beginOfDay);
            map.put("end", endOfDay);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        // 用于存放所有日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            Map mapNew = new HashMap<>();
            Map mapTotal = new HashMap<>();
            mapNew.put("begin", beginOfDay);
            mapNew.put("end", endOfDay);
            mapTotal.put("end", endOfDay);
            Integer newUser = userMapper.countByMap(mapNew);
            newUser = newUser == null ? 0 : newUser;
            Integer totalUser = userMapper.countByMap(mapTotal);
            totalUser = totalUser == null ? 0 : totalUser;
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        // 用于存放所有日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            Map mapTotal = new HashMap<>();
            Map mapValid = new HashMap<>();
            mapTotal.put("begin", beginOfDay);
            mapTotal.put("end", endOfDay);
            mapValid.put("begin", beginOfDay);
            mapValid.put("end", endOfDay);
            mapValid.put("status", Orders.COMPLETED);
            Integer totalOrder = orderMapper.countByMap(mapTotal);
            Integer validOrder = orderMapper.countByMap(mapValid);
            totalOrder = totalOrder == null ? 0 : totalOrder;
            validOrder = validOrder == null ? 0 : validOrder;
            orderCountList.add(totalOrder);
            validOrderCountList.add(validOrder);
        }

        Map mapTotal = new HashMap<>();
        Map mapValid = new HashMap<>();
        mapTotal.put("end", LocalDateTime.of(end, LocalTime.MAX));
        mapValid.put("end", LocalDateTime.of(end, LocalTime.MAX));
        mapValid.put("status", Orders.COMPLETED);
        Integer totalOrderCount = orderMapper.countByMap(mapTotal);
        Integer validOrderCount = orderMapper.countByMap(mapValid);
        Double orderCompletionRate = validOrderCount * 1.0 / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        Map map = new HashMap<>();
        map.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("end", LocalDateTime.of(end, LocalTime.MAX));
        List<GoodsSalesDTO> goodsSalesDTO = orderMapper.top10(map);
        List<String> nameList = goodsSalesDTO.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTO.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出报表
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {

        // 查数据库获得营业数据
        LocalDate now = LocalDate.now();
        LocalDate begin = now.minusDays(30);
        LocalDate end = now.minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));
        // 写入excel
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("tempate/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);

            // 填充数据--时间
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间:" + begin + "-" + end);

            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());

            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 29; i >= 0; i--) {
                LocalDate date = begin.plusDays(i);
                BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());
            }

            // 导出报表
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();
            inputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
