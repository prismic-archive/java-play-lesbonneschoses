package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import views.html.*;

import io.prismic.*;
import static controllers.Prismic.*;

public class Application extends Controller {

  // -- Resolve links to documents
  final public static LinkResolver linkResolver(Api api, String ref, Http.Request request) {
    return new LinkResolver(api, ref, request);
  } 

  public static class LinkResolver extends DocumentLinkResolver {
    final Api api;
    final String ref;
    final Http.Request request;

    public LinkResolver(Api api, String ref, Http.Request request) {
      this.api = api;
      this.ref = ref;
      this.request = request;
    }

    public String resolve(Fragment.DocumentLink link) {

      // For "Bookmarked" documents that use a special page
      if(isBookmark(api, link, "about")) {
        return routes.Application.about(ref).absoluteURL(request);
      }
      else if(isBookmark(api, link, "jobs")) {
        return routes.Application.jobs(ref).absoluteURL(request);
      }
      else if(isBookmark(api, link, "stores")) {
        return routes.Application.stores(ref).absoluteURL(request);
      }

      // Store documents
      else if("store".equals(link.getType()) && !link.isBroken()) {
        return routes.Application.storeDetail(link.getId(), link.getSlug(), ref).absoluteURL(request);
      }

      // Any product
      else if("product".equals(link.getType()) && !link.isBroken()) {
        return routes.Application.productDetail(link.getId(), link.getSlug(), ref).absoluteURL(request);
      } 

      // Job offers
      else if("job-offer".equals(link.getType()) && !link.isBroken()) {
        return routes.Application.jobDetail(link.getId(), link.getSlug(), ref).absoluteURL(request);
      } 

      // Blog
      else if("blog-post".equals(link.getType()) && !link.isBroken()) {
        return routes.Application.blogPost(link.getId(), link.getSlug(), ref).absoluteURL(request);
      } 

      // Any other link
      return routes.Application.brokenLink(ref).absoluteURL(request);
    }
  }

  // -- Page not found
  public static Result pageNotFound() {
    return notFound(views.html.pageNotFound.render());
  }

  @Prismic.Action
  public static Result brokenLink(String ref) {
    return pageNotFound();
  }

  // -- Home page

  @Prismic.Action
  public static Result index(String ref) {
    List<Document> products = prismic().getApi().getForm("products").ref(prismic().getRef()).submit();
    List<Document> featured = prismic().getApi().getForm("featured").ref(prismic().getRef()).submit();
    return ok(views.html.index.render(products, featured));
  }

  // -- About us

  @Prismic.Action
  public static Result about(String ref) {
    Document aboutPage = prismic().getBookmark("about");
    if(aboutPage == null) {
      return pageNotFound();
    } else {
      return ok(views.html.about.render(aboutPage));
    }
  }

  // -- Jobs

  @Prismic.Action
  public static Result jobs(String ref) {
    Document jobsPage = prismic().getBookmark("jobs");
    if(jobsPage == null) {
      return pageNotFound();
    } else {
      List<Document> jobs = prismic().getApi().getForm("jobs").ref(prismic().getRef()).submit();
      return ok(views.html.jobs.render(jobsPage, jobs));
    }
  }

  @Prismic.Action
  public static Result jobDetail(String id, String slug, String ref) {
    Document jobsPage = prismic().getBookmark("jobs");
    if(jobsPage == null) {
      return pageNotFound();
    } else {
      Document job = prismic().getDocument(id);
      String checked = prismic().checkSlug(job, slug);
      if(checked == DOCUMENT_NOT_FOUND) {
        return pageNotFound();
      }
      else if(checked == null) {
        return ok(views.html.jobDetail.render(jobsPage, job));
      }
      else {
        return redirect(routes.Application.jobDetail(id, checked, ref));
      }
    }
  }

  // -- Stores

  @Prismic.Action
  public static Result stores(String ref) {
    Document storesPage = prismic().getBookmark("stores");
    if(storesPage == null) {
      return pageNotFound();
    } else {
      List<Document> stores = prismic().getApi().getForm("stores").ref(prismic().getRef()).submit();
      return ok(views.html.stores.render(storesPage, stores));
    }
  }

  @Prismic.Action
  public static Result storeDetail(String id, String slug, String ref) {
    Document store = prismic().getDocument(id);
    String checked = prismic().checkSlug(store, slug);
    if(checked == DOCUMENT_NOT_FOUND) {
      return pageNotFound();
    }
    else if(checked == null) {
      return ok(views.html.storeDetail.render(store));
    }
    else {
      return redirect(routes.Application.storeDetail(id, checked, ref));
    }
  }

  // -- Products Selections

  @Prismic.Action
  public static Result selectionDetail(String id, String slug, String ref) {
    Document selection = prismic().getDocument(id);
    String checked = prismic().checkSlug(selection, slug);
    if(checked == DOCUMENT_NOT_FOUND) {
      return pageNotFound();
    }
    else if(checked == null) {
      List<String> productIds = new ArrayList<String>();
      for(Fragment fragment: selection.getAll("selection.product")) {
        Fragment.DocumentLink link = (Fragment.DocumentLink)fragment;
        if(link.getType().equals("product") && !link.isBroken()) {
          productIds.add(link.getId());
        }
      }
      List<Document> products = prismic().getDocuments(productIds);
      return ok(views.html.selectionDetail.render(selection, products));
    }
    else {
      return redirect(routes.Application.selectionDetail(id, checked, ref));
    }
  }

  // -- Blog

  final public static String[] BlogCategories = new String[] {
    "Announcements", 
    "Do it yourself", 
    "Behind the scenes"
  };

  @Prismic.Action
  public static Result blog(String category, String ref) {
    List<Document> posts;
    if(category == null) {
      posts = prismic().getApi().getForm("blog").ref(prismic().getRef()).submit();
    } else {
      posts = prismic().getApi().getForm("blog").query("[[:d = at(my.blog-post.category, \"" + category + "\")]]").ref(prismic().getRef()).submit();
    }
    Collections.sort(posts, new Comparator<Document>() {
      public int compare(Document d1, Document d2) {
        Long t1 = d1.getDate("blog-post.date") == null ? 0 : d1.getDate("blog-post.date").getValue().getMillis();
        Long t2 = d2.getDate("blog-post.date") == null ? 0 : d2.getDate("blog-post.date").getValue().getMillis();
        return t2.compareTo(t1);
      }
    });
    return ok(views.html.posts.render(posts));
  }

  @Prismic.Action
  public static Result blogPost(String id, String slug, String ref) {
    Document post = prismic().getDocument(id);
    String checked = prismic().checkSlug(post, slug);
    if(checked == DOCUMENT_NOT_FOUND) {
      return pageNotFound();
    }
    else if(checked == null) {
      List<String> productIds = new ArrayList<String>();
      for(Fragment fragment: post.getAll("blog-post.relatedproduct")) {
        Fragment.DocumentLink link = (Fragment.DocumentLink)fragment;
        if(link.getType().equals("product") && !link.isBroken()) {
          productIds.add(link.getId());
        }
      }
      List<Document> relatedProducts = prismic().getDocuments(productIds);

      List<String> postIds = new ArrayList<String>();
      for(Fragment fragment: post.getAll("blog-post.relatedpost")) {
        Fragment.DocumentLink link = (Fragment.DocumentLink)fragment;
        if(link.getType().equals("blog-post") && !link.isBroken()) {
          postIds.add(link.getId());
        }
      }
      List<Document> relatedPosts = prismic().getDocuments(postIds);

      return ok(views.html.postDetail.render(post, relatedProducts, relatedPosts));
    }
    else {
      return redirect(routes.Application.blogPost(id, checked, ref));
    }
  }

  // -- Products

  final public static Map<String,String> ProductCategories = new LinkedHashMap<String,String>();
  static {
    ProductCategories.put("Macaron", "Macarons");
    ProductCategories.put("Cupcake", "Cup Cakes");
    ProductCategories.put("Pie", "Little Pies");
  }

  @Prismic.Action
  public static Result products(String ref) {
    List<Document> products = prismic().getApi().getForm("products").ref(prismic().getRef()).submit();
    return ok(views.html.products.render(products, null));
  }

  @Prismic.Action
  public static Result productDetail(String id, String slug, String ref) {
    Document product = prismic().getDocument(id);
    String checked = prismic().checkSlug(product, slug);
    if(checked == DOCUMENT_NOT_FOUND) {
      return pageNotFound();
    }
    else if(checked == null) {
      List<String> productIds = new ArrayList<String>();
      for(Fragment fragment: product.getAll("product.related")) {
        Fragment.DocumentLink link = (Fragment.DocumentLink)fragment;
        if(link.getType().equals("product") && !link.isBroken()) {
          productIds.add(link.getId());
        }
      }
      List<Document> relatedProducts = prismic().getDocuments(productIds);

      return ok(views.html.productDetail.render(product, relatedProducts));
    }
    else {
      return redirect(routes.Application.productDetail(id, checked, ref));
    }
  }

  @Prismic.Action
  public static Result productsByFlavour(String flavour, String ref) {
    List<Document> products = prismic().getApi().getForm("everything").query("[[:d = at(my.product.flavour, \"" + flavour + "\")]]").ref(prismic().getRef()).submit();
    if(products.isEmpty()) {
      return pageNotFound();
    } else {
      return ok(views.html.productsByFlavour.render(flavour, products));
    }
  }

  // -- Search

  @Prismic.Action
  public static Result search(String query, String ref) {
    if(query == null || query.trim().isEmpty()) {
      return ok(views.html.search.render(null, null, null));
    } else {
      List<Document> products = prismic().getApi().getForm("everything").query("[[:d = any(document.type, [\"product\", \"selection\"])][:d = fulltext(document, \"" + query + "\")]]").ref(prismic().getRef()).submit();
      List<Document> others = prismic().getApi().getForm("everything").query("[[:d = any(document.type, [\"article\", \"blog-post\", \"job-offer\", \"store\"])][:d = fulltext(document, \"" + query + "\")]]").ref(prismic().getRef()).submit();
      return ok(views.html.search.render(query, products, others));
    }
  }
  
}
