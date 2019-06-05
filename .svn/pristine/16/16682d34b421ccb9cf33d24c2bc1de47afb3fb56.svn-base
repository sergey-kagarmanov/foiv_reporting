package application.models;

public class Action {

	private Integer id;
	private String name;

	public Action() {
		this(null, null);
	}
	
	public Action (Integer id, String name){
		this.id = id;
		this.name = name;
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Action){
			return ((Action)obj).getId().equals(id);
		}else{
			return false;
		}
	}

	@Override
	public String toString() {
		return name;
	}

}
