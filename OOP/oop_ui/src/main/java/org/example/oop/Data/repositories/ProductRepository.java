package org.example.oop.Data.Repositories;

import org.example.oop.Data.models.Product;
import org.example.oop.Data.storage.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductRepository - quản lý lưu/đọc Product từ file products.txt.
 *
 * Mục đích:
 * - Cung cấp các thao tác CRUD cơ bản cho Product: save, findById, findAll, update, delete, exists, count.
 * - File storage đơn giản: mỗi dòng một product theo định dạng Product.toFileFormat().
 *
 * Ghi chú chi tiết cho người duy trì:
 * - Định dạng file: toFileFormat() và fromFileFormat() trong Product phải đồng bộ hoàn toàn.
 * - Khi đọc file, repository bỏ qua dòng rỗng và dòng bắt đầu bằng '#' để hỗ trợ comment trong file dữ liệu.
 * - Các thao tác update/delete đọc toàn bộ file vào bộ nhớ, chỉnh sửa và ghi lại toàn bộ file.
 *   - Ưu điểm: đơn giản để triển khai.
 *   - Hạn chế: không phù hợp file lớn, có thể tốn bộ nhớ/time I/O.
 * - Concurrency: hiện không có cơ chế lock; nếu nhiều tiến trình/luồng cùng ghi file, dữ liệu có thể bị ghi đè.
 *   - Nếu cần chạy đa tiến trình, thêm lock trên file (ví dụ FileChannel.lock) hoặc sử dụng DB.
 * - Error handling: các IOException khi đọc/ghi được bọc thành RuntimeException để caller biết có lỗi I/O.
 *
 * Lời khuyên cải tiến:
 * - Nếu hệ thống mở rộng, chuyển sang DB (SQLite/Postgres) để hỗ trợ transaction và truy vấn hiệu quả.
 * - Thêm unit tests cho từFile/toFile format để đảm bảo tương thích khi thay đổi model.
 */
public class ProductRepository implements DataRepository<Product> {
    private static final String FILENAME = "products.txt";
    private final FileManager fileManager;

    public ProductRepository() {
        this.fileManager = new FileManager();
    }

    @Override
    public Product save(Product product) {
        try {
            if (exists(product.getId())) {
                update(product);
            } else {
                fileManager.appendLine(FILENAME, product.toFileFormat());
            }
            return product;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save product", e);
        }
    }

    @Override
    public Optional<Product> findById(int id) {
        return findAll().stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    @Override
    public List<Product> findAll() {
        try {
            return fileManager.readLines(FILENAME).stream()
                    .filter(line -> line != null && !line.trim().isEmpty() && !line.trim().startsWith("#"))
                    .map(Product::fromFileFormat)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void update(Product product) {
        try {
            List<String> lines = fileManager.readLines(FILENAME);
            List<String> updated = lines.stream()
                    .map(line -> {
                        if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) {
                            return line;
                        }
                        Product p = Product.fromFileFormat(line);
                        return p.getId() == product.getId() ? product.toFileFormat() : line;
                    })
                    .collect(Collectors.toList());
            fileManager.writeLines(FILENAME, updated);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            List<String> lines = fileManager.readLines(FILENAME);
            List<String> filtered = lines.stream()
                    .filter(line -> {
                        if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) {
                            return true;
                        }
                        return Product.fromFileFormat(line).getId() != id;
                    })
                    .collect(Collectors.toList());
            fileManager.writeLines(FILENAME, filtered);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    @Override
    public boolean exists(int id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        return findAll().size();
    }
}
