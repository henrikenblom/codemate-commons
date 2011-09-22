package se.codemate.spring.pagination;

import java.util.Map;

/**
 * User: bogghed
 * Date: Apr 23, 2008
 * Time: 2:20:21 PM
 *
 * @author Erik Bogghed <erik.bogghed@forefront.se>
 */
public class Pagination {

    private int pageNo = 1;
    private int pageSize = 10;
    private String orderBy;
    private String direction = "DESC";

    public Pagination(Map parameterMap) {
        pageNo = PaginationUtil.getIntValue("pageNo", parameterMap, 1);
        pageSize = PaginationUtil.getIntValue("pageSize", parameterMap, 10);
        orderBy = PaginationUtil.getStringValue("orderBy", parameterMap, null);
        direction = PaginationUtil.getStringValue("direction", parameterMap, "DESC");
    }

    public int getPageNo() {
        return pageNo;
    }

    public Pagination setPageNo(int pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Pagination setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public Pagination setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public String getDirection() {
        return direction;
    }

    public Pagination setDirection(String direction) {
        this.direction = direction;
        return this;
    }
}
