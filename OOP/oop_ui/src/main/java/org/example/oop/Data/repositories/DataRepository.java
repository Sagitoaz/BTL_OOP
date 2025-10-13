package org.example.oop.Data.Repositories;

import java.util.List;
import java.util.Optional;

/**
 * DataRepository - Interface chung cho các repository.
 * Định nghĩa các thao tác CRUD cơ bản cho các entity.
 */
public interface DataRepository<T> {

    /**
     * Lưu entity. Nếu entity đã tồn tại (dựa trên ID), cập nhật; ngược lại, tạo mới.
     * @param entity Entity cần lưu
     * @return Entity đã được lưu
     */
    T save(T entity);

    /**
     * Tìm entity theo ID.
     * @param id ID của entity
     * @return Optional chứa entity nếu tìm thấy, rỗng nếu không
     */
    Optional<T> findById(int id);

    /**
     * Lấy tất cả entities.
     * @return Danh sách tất cả entities
     */
    List<T> findAll();

    /**
     * Cập nhật entity.
     * @param entity Entity cần cập nhật
     */
    void update(T entity);

    /**
     * Xóa entity theo ID.
     * @param id ID của entity cần xóa
     */
    void delete(int id);

    /**
     * Kiểm tra xem entity có tồn tại theo ID không.
     * @param id ID của entity
     * @return true nếu tồn tại, false nếu không
     */
    boolean exists(int id);

    /**
     * Đếm số lượng entities.
     * @return Số lượng entities
     */
    long count();
}
