package se.codemate.spring.pagination;

import java.util.List;
import java.util.ArrayList;

/**
 * User: bogghed
 * Date: Apr 23, 2008
 * Time: 2:19:12 PM
 *
 * @author Erik Bogghed <erik.bogghed@forefront.se>
 */
public class Page<E> {

    private int pageNumber;
    private int pagesAvailable;
    private List<E> pageItems = new ArrayList<E>();

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPagesAvailable(int pagesAvailable) {
        this.pagesAvailable = pagesAvailable;
    }

    public void setPageItems(List<E> pageItems) {
        this.pageItems = pageItems;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPagesAvailable() {
        return pagesAvailable;
    }

    public List<E> getPageItems() {
        return pageItems;
    }
}
