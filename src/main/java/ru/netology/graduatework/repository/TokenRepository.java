package ru.netology.graduatework.repository;

import org.springframework.data.repository.CrudRepository;
import ru.netology.graduatework.entities.TokenEntity;

@org.springframework.stereotype.Repository
public interface TokenRepository extends CrudRepository<TokenEntity, String> {
}