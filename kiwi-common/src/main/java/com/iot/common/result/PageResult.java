package com.iot.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页结果
 * @author wan
 */
@Schema(description = "分页响应结果")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
public class PageResult<T> {
    @Schema(description = "列表数据")
    private List<T> list;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;

    @Schema(description = "总页数", example = "10")
    private Integer totalPages;

    /**
     * 将分页信息封装到统一的接口
     */
    public static <T> PageResult<T> restPage(Page<T> pageInfo) {
        PageResult<T> result = new PageResult<>();
        result.setList(pageInfo.getContent());
        result.setTotal(pageInfo.getTotalElements());
        result.setPageNum(pageInfo.getNumber() + 1);
        result.setPageSize(pageInfo.getSize());
        result.setTotalPages(pageInfo.getTotalPages());
        return result;
    }
}
