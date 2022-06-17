package domain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Course {
	private String id;
	private String name;
	private int units;
	
	private List<Course> prerequisites;

	public Course(String id, String name, int units) {
		this.id = id;
		this.name = name;
		this.units = units;
		prerequisites = new ArrayList<Course>();
	}
	
	public void addPre(Course c) {
		prerequisites.add(c);
	}

	public Course withPre(Course... pres) {
		prerequisites.addAll(Arrays.asList(pres));
		return this;
	}

	public List<Course> getPrerequisites() {
		return prerequisites;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" {");
		for (int i= 0; i< prerequisites.size(); i++){
			sb.append(prerequisites.get(i).getName());
			if (i< prerequisites.size()-1)
				sb.append(", ");
		}
		sb.append("}");
		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public int getUnits() {
		return units;
	}

	public String getId() {
		return id;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Course))
			return false;
		Course other = (Course)obj;
		return id.equals(other.id);
	}
}
