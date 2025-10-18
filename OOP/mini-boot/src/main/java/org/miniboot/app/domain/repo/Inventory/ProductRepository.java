package org.miniboot.app.domain.repo.Inventory;

import java.nio.file.OpenOption;
import java.util.List;
import java.util.Optional;

import org.miniboot.app.domain.models.Inventory.Product;

public interface ProductRepository {
     List<Product> findAll();

     Optional<Product> findById(int id);

     boolean deleteById(int id);

     Product save(Product product);
}
