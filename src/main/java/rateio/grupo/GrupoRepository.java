package rateio.grupo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GrupoRepository extends JpaRepository<GrupoEntidade, UUID> {

}
