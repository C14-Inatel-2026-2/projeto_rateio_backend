package rateio.grupo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rateio.grupo.entity.GrupoEntidade;

import java.util.UUID;

public interface GrupoRepository extends JpaRepository<GrupoEntidade, UUID> {

}
