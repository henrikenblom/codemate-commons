package se.codemate.spring.pagination;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.dao.DataAccessException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * User: bogghed
 * Date: Apr 23, 2008
 * Time: 2:21:43 PM
 *
 * @author Erik Bogghed <erik.bogghed@forefront.se>
 */
public class PaginationHelper<E> {

    private static Log log = LogFactory.getLog(PaginationHelper.class);

    public Page<E> fetchPage(
            final JdbcTemplate jdbcTemplate,
            final String sqlCountRows,
            final String sqlFetchRows,
            final Object args[],
            final Pagination pagination,
            final ParameterizedRowMapper<E> rowMapper) {

        final int rowCount = jdbcTemplate.queryForInt(sqlCountRows, args);
        log.debug("rowCount = " + rowCount);

        int pageCount = rowCount / pagination.getPageSize();
        log.debug("pageCount = " + pageCount);
        
        if (rowCount > pagination.getPageSize() * pageCount) {
            pageCount++;
            log.debug("Incrementing pageCount");
        }

        final Page<E> page = new Page<E>();
        page.setPageNumber(pagination.getPageNo());
        page.setPagesAvailable(pageCount);

        if (rowCount > 0) {
            final StringBuilder sqlFetchRowsWithOrderBy = new StringBuilder(sqlFetchRows);

            if (pagination.getOrderBy() != null) {

                sqlFetchRowsWithOrderBy
                        .append(" ORDER BY ")
                        .append(pagination.getOrderBy());

                if (pagination.getDirection() != null) {
                    sqlFetchRowsWithOrderBy
                            .append(" ")
                            .append(pagination.getDirection());
                }
            }

            final int startRow = (pagination.getPageNo() - 1) * pagination.getPageSize();

            jdbcTemplate.query(
                    sqlFetchRows,
                    args,
                    new ResultSetExtractor() {
                        public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
                            final List<E> pageItems = page.getPageItems();
                            int currentRow = 0;
                            while (rs.next() && currentRow < startRow + pagination.getPageSize()) {
                                if (currentRow >= startRow) {
                                    pageItems.add(rowMapper.mapRow(rs, currentRow));
                                }
                                currentRow++;
                            }
                            return page;
                        }
                    });
        }
        return page;
    }

}
