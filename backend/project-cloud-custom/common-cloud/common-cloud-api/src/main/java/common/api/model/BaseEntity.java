package common.api.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author asd <br>
 * @create 2021-06-30 9:23 AM <br>
 * @project custom-upms-grpc <br>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class BaseEntity<T extends Model> extends Model implements Serializable {
    protected static final long serialVersionUID = 1L;
    private Boolean isDeleted;

    @TableField(value = "inserted_time", fill = FieldFill.INSERT)
    private LocalDateTime insertedTime;

    @TableField(value = "updated_time", fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;

    @TableField(value = "inserted_by", fill = FieldFill.INSERT)
    private Long insertedBy;

    @TableField(value = "updated_by", fill = FieldFill.UPDATE)
    private Long updatedBy;

    public BaseEntity(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
