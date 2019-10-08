package com.chl.gbo.cental.bean;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: BoYanG
 * @Describe DataTable封装
 */
public class DatatablesView<T> {

    private List<T> data; //data 与datatales 加载的“dataSrc"对应

    private int recordsTotal;

    private int recordsFiltered;

    /**
     *服务器请求次数，防止XSS攻击
     */
    private int draw;

    public DatatablesView(HttpServletRequest request) {
        this.draw = DatatableRequest.draw(request);
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
        this.recordsFiltered = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }
}
