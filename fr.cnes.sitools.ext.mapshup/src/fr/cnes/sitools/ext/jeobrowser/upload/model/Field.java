package fr.cnes.sitools.ext.jeobrowser.upload.model;

/**
 * GeoJSON dataset description DTO object
 * 
 * 
 * @author m.gond
 */
public class Field {

  /**
   * id : le nom de la clé correspondant au paramètre tel que défini dans le
   * modèle d'url (cf. Interfaces - $2.2.2). Cet élément est obligatoire.
   * 
   * "name" dans dictionary pour les fields de type specific
   * 
   */
  private String id;

  /**
   * title : le nom de la clé tel qu'il doit être affiché à l'utilisateur. Ce
   * nom doit être rédigé en langue anglaise. Cet élément est obligatoire.
   * 
   * "description" dans dictionary
   */
  private String title;

  /**
   * type : le type du paramètre. Les types supportés sont : date, enumeration,
   * text, number. Cet élément est obligatoire.
   * 
   * ""
   */
  private String type;

  /**
   * size : la taille en nombre de caractères que doit avoir la boite de saisie
   * textuel pour ce paramètre sur jeobrowser. Cet élément ne s'applique qu'aux
   * types text et number. Cet élément est optionnel.
   */
  private String size;

  /**
   * operator : l'opérateur de comparaison applicable à ce paramètre. Les
   * valeurs possibles sont1 eq, lt, gt, bt. Cet élément ne s'applique qu'au
   * type number. Cet élément est optionnel.
   */
  private String operator;

  /**
   * population : le nombre total de résultats du jeu de données. Cet élément ne
   * s'applique qu'au type enumeration. Cet élément est optionnel /*
   */
  private int population;

  /**
   * son : la liste des énumérations possible pour le paramètre. Cet élément ne
   * s'applique qu'au type enumeration. Cet élément est optionnel
   */
  private String son;

  /**
   * value : la valeur par défaut du paramètre. Cet élément est optionnel.
   */
  private String value;

  /**
   * unique : cet élément ne s'applique qu'au type enumeration. C'est un booléen
   * qui indique si plusieurs élements de l'énumération peuvent être séléctionné
   * par l'utilisateur (unique:false) ou bien si la sélection est exclusive,
   * c'est à dire qu'un seul élément peut être séléctionné par l'utilisateur
   * (unique:false). Cet élément est optionnel et vaut false par défaut si il
   * n'est pas spécifié.
   */
  private boolean unique;

  /**
   * Gets the id value
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of id
   * 
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the title value
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the value of title
   * 
   * @param title
   *          the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the type value
   * 
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the value of type
   * 
   * @param type
   *          the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the size value
   * 
   * @return the size
   */
  public String getSize() {
    return size;
  }

  /**
   * Sets the value of size
   * 
   * @param size
   *          the size to set
   */
  public void setSize(String size) {
    this.size = size;
  }

  /**
   * Gets the operator value
   * 
   * @return the operator
   */
  public String getOperator() {
    return operator;
  }

  /**
   * Sets the value of operator
   * 
   * @param operator
   *          the operator to set
   */
  public void setOperator(String operator) {
    this.operator = operator;
  }

  /**
   * Gets the population value
   * 
   * @return the population
   */
  public int getPopulation() {
    return population;
  }

  /**
   * Sets the value of population
   * 
   * @param population
   *          the population to set
   */
  public void setPopulation(int population) {
    this.population = population;
  }

  /**
   * Gets the son value
   * 
   * @return the son
   */
  public String getSon() {
    return son;
  }

  /**
   * Sets the value of son
   * 
   * @param son
   *          the son to set
   */
  public void setSon(String son) {
    this.son = son;
  }

  /**
   * Gets the value value
   * 
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value of value
   * 
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Gets the unique value
   * 
   * @return the unique
   */
  public boolean isUnique() {
    return unique;
  }

  /**
   * Sets the value of unique
   * 
   * @param unique
   *          the unique to set
   */
  public void setUnique(boolean unique) {
    this.unique = unique;
  }

}
