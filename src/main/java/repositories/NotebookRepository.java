package repositories;

import models.Notebook;

import java.util.List;
import java.util.Optional;

public interface NotebookRepository extends CrudRepository<Notebook> {
    List<Notebook> findByChatId (Long chatId);
    Optional<Notebook> findBySNP (Long chatId, String surname, String name, String patronymic);
}
