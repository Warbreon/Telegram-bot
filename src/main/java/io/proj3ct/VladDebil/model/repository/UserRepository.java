package io.proj3ct.VladDebil.model.repository;

import io.proj3ct.VladDebil.model.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

}
