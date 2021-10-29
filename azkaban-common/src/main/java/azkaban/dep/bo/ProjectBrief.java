package azkaban.dep.bo;

import java.util.Objects;

public class ProjectBrief {
    private int id;
    private String name;

    public ProjectBrief(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectBrief that = (ProjectBrief) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProjectBrief{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
