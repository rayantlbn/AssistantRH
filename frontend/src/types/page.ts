/** Réponse paginée de l'API : { content, page: { size, number, totalElements, totalPages } }. */
export interface Page<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}
