package ru.netology.graduatework.repository;

import org.springframework.data.repository.CrudRepository;
import ru.netology.graduatework.entities.UserEntity;

@org.springframework.stereotype.Repository
public interface UserRepository extends CrudRepository<UserEntity, String> {
}